package com.cloudticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.Cache.CacheData;
import com.cloudticket.common.constant.AuditConstant;
import com.cloudticket.common.constant.CacheKey;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.AuditLogMapper;
import com.cloudticket.mapper.SessionMapper;
import com.cloudticket.mapper.ShowInfoMapper;
import com.cloudticket.mapper.StockMapper;
import com.cloudticket.mapper.TicketCategoryMapper;
import com.cloudticket.pojo.dto.*;
import com.cloudticket.pojo.entity.*;
import com.cloudticket.pojo.enums.SessionStatus;
import com.cloudticket.pojo.enums.ShowCategory;
import com.cloudticket.pojo.enums.ShowStatus;
import com.cloudticket.pojo.vo.*;
import com.cloudticket.service.ShowService;
import com.cloudticket.service.VenueService;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShowServiceImpl extends ServiceImpl<ShowInfoMapper, ShowInfo> implements ShowService {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    ShowInfoMapper showInfoMapper;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    Cache<Long, UserShowDetailVO> userShowVOCache;

    @Autowired
    SessionMapper sessionMapper;

    @Autowired
    @Resource(name = "showBloomFilter")
    RBloomFilter rBloomFilter;

    @Autowired
    TicketCategoryMapper ticketCategoryMapper;

    @Autowired
    StockMapper stockMapper;

    @Autowired
    AuditLogMapper auditLogMapper;

    @Autowired
    VenueService venueService;

    @Override
    public PageResult<AdminShowVO> showAdminPage(ShowPageQuery showPageQuery) {
        Page<AdminShowVO> page = new Page<>(showPageQuery.getPageNum(), showPageQuery.getPageSize());
        showInfoMapper.selectShowPage(page, showPageQuery.getStatus());
        List<AdminShowVO> rows = page.getRecords().stream().map(vo -> {
            vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());   // 列表也要状态翻译
            vo.setCategoryDesc(ShowCategory.of(vo.getCategory()).getLabel());   // 分类翻译
            return vo;
        }).toList();
        return new PageResult<>((int) page.getTotal(), rows);
    }

    @Override
    public AdminShowDetailVO showAdminDetail(Long id) {
        // ① 演出 + 主办方名称：一条 SQL（LEFT JOIN merchant），直接映射 VO
        AdminShowDetailVO vo = showInfoMapper.selectAuditDetail(id);
        if (vo == null) throw new BaseException("演出不存在");
        vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());
        vo.setCategoryDesc(ShowCategory.of(vo.getCategory()).getLabel());

        // ② 场次 × 票档 × 库存 组装（平台详情 / 主办方详情共用）
        //统一根据演出组装出每个场次的各个票档对应库存信息
        List<SessionVO> sessions = assembleSessions(id);
        vo.setSessions(sessions);

        // ③ 审核历史：一条联表 SQL（admin 补名字 + JSON 函数直接解析 params）
        vo.setAuditRecords(auditLogMapper.selectShowAuditRecords(id));
        return vo;
    }


    @Audit(action = AuditConstant.SHOW_AUDIT, targetType = AuditConstant.TARGET_SHOW)
    @Override
    public void showAudit(Long id, AdminShowAuditDTO showAuditDTO) {
        // ⓪ 演出必须存在（保证后续校验/提示准确）
        if (getById(id) == null) {
            throw new BaseException("演出不存在");
        }

        // ① 驳回必须填原因
        if (!showAuditDTO.getApproved() && StrUtil.isBlank(showAuditDTO.getRemark())) {
            throw new BaseException("驳回必须填写审核原因");
        }

        // ② 通过前完整性校验（宽松：每个场次至少一个票档有库存）
        if (showAuditDTO.getApproved()) {
            checkShowComplete(id);
        }

        // ③ CAS：只审"待审核"的演出（通过=3 / 驳回=2）；驳回原因写 audit_remark，通过时清空
        boolean success = lambdaUpdate()
                .set(ShowInfo::getStatus, showAuditDTO.getApproved()
                        ? ShowStatus.APPROVED.getCode()
                        : ShowStatus.REJECTED.getCode())
                .set(ShowInfo::getAuditRemark,
                        showAuditDTO.getApproved() ? null : showAuditDTO.getRemark())
                .eq(ShowInfo::getStatus, ShowStatus.PENDING_REVIEW.getCode())
                .eq(ShowInfo::getId, id)
                .update();
        if (!success) {
            throw new BaseException("该演出不存在或已审核");
        }
        // ① 审核通过 → 新演出必须进布隆（启动预热只加了"启动时已有的 id"）
        if (showAuditDTO.getApproved()) {
            rBloomFilter.add(id);
        }

        // ② 主动失效详情缓存（Redis + 本地）
        evictDetailCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)   // 三张表一起写：任何一步失败全回滚
    public IdVO showcreate(MerchantShowSaveDTO showCreateDTO) {
        validDate(showCreateDTO);                                                  // 有效性校验
        ShowInfo show = createShow(showCreateDTO, getTotalStock(showCreateDTO));   // 写 show_info
        insertShowStructure(show.getId(), showCreateDTO);                          // 写票档 + 场次 + 库存
        return new IdVO(show.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void showUpdate(Long id, MerchantShowSaveDTO showCreateDTO) {
        ShowInfo show = getById(id);
        if (show == null) {
            throw new BaseException("未找到该演出");
        }
        // 先校验归属，再看状态（避免泄露"别人的演出处于什么状态"）
        if (!BaseContext.getCurrentId().equals(show.getMerchantId())) {
            throw new BaseException("您目前没有修改该演出权限");
        }
        Integer status = show.getStatus();
        if (status != ShowStatus.DRAFT.getCode() && status != ShowStatus.REJECTED.getCode()) {
            throw new BaseException("当前状态不可修改");
        }
        validDate(showCreateDTO);

        // 删旧：先查场次 id 集合 → 删库存 → 删票档 → 删场次
        List<Long> oldSessionIds = sessionMapper.selectList(
                        new LambdaQueryWrapper<ShowSession>().eq(ShowSession::getShowId, id))
                .stream().map(ShowSession::getId).toList();
        if (!oldSessionIds.isEmpty()) {
            stockMapper.delete(new LambdaQueryWrapper<SessionCategoryStock>()
                    .in(SessionCategoryStock::getSessionId, oldSessionIds));
        }
        ticketCategoryMapper.delete(new LambdaQueryWrapper<TicketCategory>().eq(TicketCategory::getShowId, id));
        sessionMapper.delete(new LambdaQueryWrapper<ShowSession>().eq(ShowSession::getShowId, id));


        // 更新 show_info（只更新，不新增；status 保持不变）
        BeanUtils.copyProperties(showCreateDTO, show);   // title/category/coverUrl/detail/guide/venueId
        show.setTotalStock(getTotalStock(showCreateDTO));
        updateById(show);

        // 重建子表（票档 + 场次 + 库存）
        insertShowStructure(id, showCreateDTO);
        //删除redis和本地缓存,最后
        evictDetailCache(show.getId());
    }

    @Override
    public PageResult<MerchantShowVO> showMerchantPage(ShowPageQuery showPageQuery) {
        Long currentId = BaseContext.getCurrentId();
        Integer status = showPageQuery.getStatus();
        Page<ShowInfo> page=new Page<>(showPageQuery.getPageNum(),showPageQuery.getPageSize());
        page=lambdaQuery()
                .eq(ShowInfo::getMerchantId,currentId)
                .eq(status!=null,ShowInfo::getStatus,status)
                .page(page);
        List<MerchantShowVO> list = page.getRecords().stream().map(showInfo -> {
            MerchantShowVO showMerchantVO = new MerchantShowVO();
            BeanUtils.copyProperties(showInfo, showMerchantVO);
            showMerchantVO.setStatusDesc(ShowStatus.of(showInfo.getStatus()).getDesc());
            showMerchantVO.setCategoryDesc(ShowCategory.of(showInfo.getCategory()).getLabel());
            return showMerchantVO;
        }).toList();
        return new PageResult<>((int)page.getTotal(),list);
    }

    @Override
    public MerchantShowDetailVO showMerchantDetail(Long id) {
        Long merchantId = BaseContext.getCurrentId();
        MerchantShowDetailVO vo=showInfoMapper.selectMerchantDetail(id,merchantId);
        if (vo==null){
            throw new BaseException("该演出不存在");
        }
        vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());
        vo.setCategoryDesc(ShowCategory.of(vo.getCategory()).getLabel());
        List<SessionVO> sessionVOS = assembleSessions(id);
        vo.setSessions(sessionVOS);
        vo.setAuditRecords(auditLogMapper.selectShowAuditRecords(id));
        return vo;
    }

    @Override
    public void showReview(Long id) {
        Boolean success=lambdaUpdate()
                .set(ShowInfo::getStatus,ShowStatus.PENDING_REVIEW.getCode())
                .set(ShowInfo::getAuditRemark,null)
                .eq(ShowInfo::getId,id)
                .eq(ShowInfo::getMerchantId,BaseContext.getCurrentId())
                .in(ShowInfo::getStatus,ShowStatus.DRAFT.getCode(),
                        ShowStatus.REJECTED.getCode())
                .update();
        if (!success) {
            throw new BaseException("该演出不存在或当前状态不可提交（仅草稿/驳回可提交）");
        }
    }

    @Override
    public void showOffSale(Long id) {
        Boolean success=lambdaUpdate()
                .set(ShowInfo::getStatus,ShowStatus.OFF_SALE.getCode())
                // 审核通过（未开售）或销售中，都允许下架
                .in(ShowInfo::getStatus,ShowStatus.APPROVED.getCode(),ShowStatus.ON_SALE.getCode())
                .eq(ShowInfo::getMerchantId,BaseContext.getCurrentId())
                .eq(ShowInfo::getId,id)
                .update();
        if(!success){
            throw new BaseException("该演出不存在或当前状态不可下架");
        }

        evictDetailCache(id);
    }

    @Override
    public List<CategoryVO> categoryList() {
        return ShowCategory.all().stream()
                .map(c -> new CategoryVO(c.getCode(), c.getLabel()))
                .toList();
    }

    @Override
    public PageResult<UserShowVO> showUserPage(UserShowPageQuery userShowPageQuery) {
        // ===== 缓存策略（按"可复用性"分层）=====
        // ① 关键词搜索 = 无限组合/长尾 → 不缓存，直查 DB（P6 演进为 ES）
        // ② 深页（pageNum > 50）= 长尾 → 不缓存
        // ③ 其余（默认/城市/分类/翻页）= 有限组合、高频复用 → 结果缓存
        if (StrUtil.isNotBlank(userShowPageQuery.getKeyword())
                || (userShowPageQuery.getPageNum() != null && userShowPageQuery.getPageNum() > 50)) {
            return queryUserShowFromDb(userShowPageQuery);
        }

        // 缓存 key：city:category:pageNum:pageSize（keyword 必为空，不占位）
        String key = CacheKey.SHOW_LIST
                + Objects.toString(userShowPageQuery.getCity(), "-") + ":"
                + Objects.toString(userShowPageQuery.getCategory(), "-") + ":"
                + userShowPageQuery.getPageNum() + ":"
                + userShowPageQuery.getPageSize();

        // 读缓存（Redis 异常降级直查 DB，缓存故障不拖垮业务）
        try {
            @SuppressWarnings("unchecked")
            PageResult<UserShowVO> cached = (PageResult<UserShowVO>) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            // 降级：读缓存失败直接走 DB
            log.warn("读演出列表缓存失败，降级直查 DB，key={}", key, e);
        }

        PageResult<UserShowVO> result = queryUserShowFromDb(userShowPageQuery);

        // 写缓存：TTL 30s + 随机抖动 0~30s（防雪崩）
        try {
            long ttl = 30 + ThreadLocalRandom.current().nextLong(30);
            redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 降级：写缓存失败不影响返回
            log.warn("写演出列表缓存失败，降级直查 DB，key={}", key, e);
        }
        return result;
    }


    @Override
    public SessionStockVO showUserStock(Long sessionId) {

        String key=CacheKey.SESSION_STOCK+sessionId;
        //优先读取缓存
        try {
            SessionStockVO sessionStockVO1 = (SessionStockVO)redisTemplate.opsForValue().get(key);
            if (sessionStockVO1!=null){
                return sessionStockVO1;
            }
        } catch (Exception e) {
            log.warn("读余量缓存失败，降级查库, sessionId={}", sessionId, e);
        }

        ShowSession session = sessionMapper.selectById(sessionId);
        if(session==null){
            throw new BaseException("该场次不存在");
        }
        Long showId = session.getShowId();
        Integer status = session.getStatus();
        SessionStockVO sessionStockVO=new SessionStockVO();
        sessionStockVO.setSessionId(sessionId);
        sessionStockVO.setStatus(status);
        sessionStockVO.setStatusDesc(SessionStatus.of(status).getDesc());
        List<CategoryRemainVO> list = ticketCategoryMapper.selectUserStock(sessionId, showId);
        // 契约 4.4：场次非"销售中"时结构正常返回，余量字段置 0（前端据 status 置灰面板）
        if (status != SessionStatus.ON_SALE.getCode()) {
            list.forEach(c -> {
                c.setRemainStock(0);
                c.setMemberRemainStock(0);
            });
        }
        sessionStockVO.setCategories(list);
        
        try {
            long ttl=3L+ThreadLocalRandom.current().nextLong(2);
            redisTemplate.opsForValue().set(key,sessionStockVO,ttl,TimeUnit.SECONDS);
        }catch (Exception e){
            log.warn("写余量缓存失败, sessionId={}", sessionId, e);
        }
        return sessionStockVO;
    }

    @Override
    public UserShowDetailVO showUserDetail(Long id) {
        //查询本地缓存,有直接返回,没有执行方法(redis和数据库),并且写回本地缓存
        return userShowVOCache.get(id, v-> loadFromRedisOrDb(id));

    }

    //用于删除本地caffeine和redis缓存
    private void evictDetailCache(Long showId) {
        try {
            redisTemplate.delete(CacheKey.SHOW_DETAIL + showId);
        } catch (Exception e) {
            log.warn("删详情缓存失败, id={}", showId, e);
        }
        userShowVOCache.invalidate(showId);
    }

    /** 演出列表查库（状态/分类翻译）—— 缓存未命中、关键词搜索、深页共用 */
    private PageResult<UserShowVO> queryUserShowFromDb(UserShowPageQuery query) {
        Page<UserShowVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        showInfoMapper.selectUserShowPage(page, query);   // XML：状态/分类/城市/关键词 + 场馆 + 最低价
        List<UserShowVO> rows = page.getRecords().stream().map(vo -> {
            vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getUserDesc());   // 用户端文案：即将开售/销售中
            vo.setCategoryDesc(ShowCategory.of(vo.getCategory()).getLabel());
            return vo;
        }).toList();
        return new PageResult<>((int) page.getTotal(), rows);
    }

    private UserShowDetailVO loadFromRedisOrDb(Long id){
        String key=CacheKey.SHOW_DETAIL+id;
        CacheData<UserShowDetailVO> data=new CacheData<>();
        //查询redis数据
        try {
            data=(CacheData<UserShowDetailVO>) redisTemplate.opsForValue().get(key);
        }catch (Exception e){
            log.warn("读详情缓存失败，降级查库, id={}", id, e);
        }
        //redis不存在,自动降级查询数据库
        if (data!=null){
            //空值缓存值
            if (data.getData()==null){
                throw new BaseException("该演出不存在");
            }
            if(data.getExpireAt() == null||data.getExpireAt().isAfter(LocalDateTime.now())){
                return data.getData();
            }
            //分布式锁
            RLock lock=redissonClient.getLock(key+":lock");
            //抢到重建,并且返回新值
            if (lock.tryLock()){
                try {
                    return selectShowBySQL(id);
                }catch (BaseException e) {
                    throw e;                              // "不存在"照抛（业务正常）
                } catch (Exception e) {
                    log.warn("重建详情失败，降级返回旧值, id={}", id, e);
                }  finally {
                    lock.unlock();
                }
            }
            //没抢到返回旧值
            return data.getData();
        }
        //检查布隆过滤器是否存在
        if (!rBloomFilter.contains(id)){
            throw new BaseException("该演出不存在");
        }
        return selectShowBySQL(id);
    }

    private  UserShowDetailVO selectShowBySQL(Long id){
        String key=CacheKey.SHOW_DETAIL+id;
        // ① 主查询：演出 + 场馆 + 价格区间（草稿/待审核/驳回不可见）
        UserShowDetailVO vo = showInfoMapper.selectUserDetail(id);
        if (vo == null) {
            //空值缓存
            try {
                redisTemplate.opsForValue().set(key,
                        new CacheData<>(null, LocalDateTime.now().plusSeconds(60)),
                        Duration.ofSeconds(60));
            } catch (Exception e) {
                log.warn("写空值缓存失败, id={}", id, e);
            }
            throw new BaseException("该演出不存在");
        }
        vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getUserDesc());
        vo.setCategoryDesc(ShowCategory.of(vo.getCategory()).getLabel());

        // ② 场次骨架（不含票档库存，余量走独立接口）
        List<ShowSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<ShowSession>()
                        .eq(ShowSession::getShowId, id)
                        .orderByAsc(ShowSession::getSessionTime));
        List<SessionSimpleVO> list = sessions.stream().map(s -> {
            SessionSimpleVO sv = new SessionSimpleVO();
            BeanUtils.copyProperties(s, sv);
            sv.setStatusDesc(SessionStatus.of(s.getStatus()).getDesc());
            return sv;
        }).toList();
        vo.setSessions(list);
        try {
            //写入缓存
            redisTemplate.opsForValue().set(key,
                    new CacheData<>(vo, LocalDateTime.now().plusMinutes(30)),
                    Duration.ofMinutes(60));
        } catch (Exception e) {
            log.warn("写详情缓存失败, id={}", id, e);
        }
        return vo;
    }

    private static int getTotalStock(MerchantShowSaveDTO showCreateDTO) {
        // ③ 总库存 = 每个场次的库存之和 × 场次数（全场统一库存）
        int perSessionStock = showCreateDTO.getCategories().stream()
                .mapToInt(c -> c.getStock() + c.getMemberStock())
                .sum();
        int totalStock = perSessionStock * showCreateDTO.getSessions().size();
        return totalStock;
    }



    /** 完整性校验（宽松）：至少一个场次，且每个场次至少有一个票档库存 > 0 */
    private void checkShowComplete(Long showId) {
        List<ShowSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<ShowSession>().eq(ShowSession::getShowId, showId));
        if (sessions.isEmpty()) {
            throw new BaseException("演出没有任何场次，不能通过审核");
        }

        List<Long> sessionIds = sessions.stream().map(ShowSession::getId).toList();
        List<SessionCategoryStock> stocks = stockMapper.selectList(
                new LambdaQueryWrapper<SessionCategoryStock>()
                        .in(SessionCategoryStock::getSessionId, sessionIds));

        //通过遍历所有的库存,得到他们的库存大于0对应的场次去重集合,如果小于场次数量说明有场次没有有库存票档
        Set<Long> sellableSessionIds = stocks.stream()
                .filter(s -> (s.getStock() != null && s.getStock() > 0)
                        || (s.getMemberStock() != null && s.getMemberStock() > 0))
                .map(SessionCategoryStock::getSessionId)
                .collect(Collectors.toSet());

        if (sellableSessionIds.size() < sessionIds.size()) {
            throw new BaseException("每个场次至少需要一个有库存的票档，不能通过审核");
        }
    }

    private void validDate(MerchantShowSaveDTO showCreateDTO) {
        // ① 场馆必须存在
        Venue venue = venueService.getById(showCreateDTO.getVenueId());
        if (venue == null) {
            throw new BaseException("该场馆不存在");
        }
        // ② 场次时间校验
        showCreateDTO.getSessions().forEach(s->{
            if (!s.getSaleStartTime().isBefore(s.getSessionTime())) {
                throw new BaseException("开售时间必须早于演出开始时间");
            }
            if (!s.getSaleEndTime().isAfter(s.getSaleStartTime())) {
                throw new BaseException("停售时间必须晚于开售时间");
            }
            if (s.getSaleStartTime().isBefore(LocalDateTime.now())) {
                throw new BaseException("开售时间不能早于当前时间");
            }
        });
        //票档名不重复
        Set<String> categoryNames = new HashSet<>();
        for (ShowSaveCategoryDTO category : showCreateDTO.getCategories()) {
            if (!categoryNames.add(category.getName())) {
                throw new BaseException("票档名不能重复：" + category.getName());
            }
        }

        // 演出分类必须是合法枚举
        if (ShowCategory.of(showCreateDTO.getCategory()) == null) {
            throw new BaseException("演出分类不合法");
        }

    }
    /** 创建演出主记录（草稿态），返回回填 id 的实体 */
    private ShowInfo createShow(MerchantShowSaveDTO showCreateDTO, int totalStock) {
        ShowInfo show = new ShowInfo();
        BeanUtils.copyProperties(showCreateDTO, show);   // title/category/coverUrl/detail/guide/venueId
        show.setMerchantId(BaseContext.getCurrentId());
        show.setStatus(ShowStatus.DRAFT.getCode());
        show.setTotalStock(totalStock);
        show.setSoldCount(0);
        save(show);                                       // 回填自增 id
        return show;
    }

    /**
     * 组装"场次 × 票档库存"嵌套列表（演出详情共用）
     * 3 条查询：场次、票档定义、库存（IN 场次集合）→ 内存笛卡尔积拼装
     */
    //查询使用
    private List<SessionVO> assembleSessions(Long showId) {
        // ① 场次：按时间升序
        List<ShowSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<ShowSession>()
                        .eq(ShowSession::getShowId, showId)
                        .orderByAsc(ShowSession::getSessionTime));

        // ② 票档定义：演出级，全场次共用（按 id 升序）
        List<TicketCategory> categories = ticketCategoryMapper.selectList(
                new LambdaQueryWrapper<TicketCategory>()
                        .eq(TicketCategory::getShowId, showId)
                        .orderByAsc(TicketCategory::getId));

        // ③ 库存：一次批量 → Map<"sessionId:categoryId", 库存行>，供内层循环 O(1) 取值
        List<Long> sessionIds = sessions.stream().map(ShowSession::getId).toList();
        Map<String, SessionCategoryStock> stockMap = sessionIds.isEmpty() ? Map.of()
                : stockMapper.selectList(new LambdaQueryWrapper<SessionCategoryStock>()
                        .in(SessionCategoryStock::getSessionId, sessionIds))
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getSessionId() + ":" + s.getCategoryId(),  // 复合 key
                        s -> s));

        // ④ 笛卡尔积组装：外层填场次，内层填票档 + 从 Map 取该组合的库存
        return sessions.stream().map(s -> {
            SessionVO sv = new SessionVO();
            BeanUtils.copyProperties(s, sv);
            sv.setStatusDesc(SessionStatus.of(s.getStatus()).getDesc());
            List<CategoryStockVO> list = categories.stream().map(c -> {
                CategoryStockVO cv = new CategoryStockVO();
                BeanUtils.copyProperties(c, cv);            // 票档定义字段
                cv.setCategoryId(c.getId());
                SessionCategoryStock st = stockMap.get(s.getId() + ":" + c.getId());
                if (st != null) {
                    BeanUtil.copyProperties(st, cv, true);  // Hutool 不支持 null source，缺行时保留 VO 默认值 0
                }
                return cv;
            }).toList();
            sv.setCategories(list);
            return sv;
        }).toList();
    }


    /** 写演出子结构：票档定义 + 场次 + 场次×票档库存（创建/编辑共用） */
    private void insertShowStructure(Long showId, MerchantShowSaveDTO showCreateDTO) {
        // 票档定义（演出级，一份）
        List<TicketCategory> categories = showCreateDTO.getCategories().stream().map(c -> {
            TicketCategory tc = new TicketCategory();
            BeanUtils.copyProperties(c, tc);              // name/price/limitPerUser
            tc.setShowId(showId);
            return tc;
        }).toList();
        categories.forEach(ticketCategoryMapper::insert);

        // 场次
        List<ShowSession> sessions = showCreateDTO.getSessions().stream().map(s -> {
            ShowSession ss = new ShowSession();
            BeanUtils.copyProperties(s, ss);              // sessionName/sessionTime/saleStartTime/saleEndTime
            ss.setShowId(showId);
            ss.setStatus(SessionStatus.NOT_ON_SALE.getCode());
            return ss;
        }).toList();
        sessions.forEach(sessionMapper::insert);

        // 笛卡尔积库存：场次 × 票档（每场库存取对应票档的值，全场统一）
        for (ShowSession ss : sessions) {
            for (int i = 0; i < categories.size(); i++) {
                TicketCategory tc = categories.get(i);
                ShowSaveCategoryDTO source = showCreateDTO.getCategories().get(i);   // 同下标对应
                SessionCategoryStock stock = new SessionCategoryStock();
                stock.setSessionId(ss.getId());
                stock.setCategoryId(tc.getId());
                stock.setStock(source.getStock());
                stock.setMemberStock(source.getMemberStock());
                stock.setSoldCount(0);
                stock.setSoldMemberCount(0);
                stock.setVersion(0);
                stockMapper.insert(stock);
            }
        }
    }

}
