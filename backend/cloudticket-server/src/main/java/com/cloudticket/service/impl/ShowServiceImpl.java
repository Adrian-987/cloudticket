package com.cloudticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.constant.AuditConstant;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.AuditLogMapper;
import com.cloudticket.mapper.SessionMapper;
import com.cloudticket.mapper.ShowInfoMapper;
import com.cloudticket.mapper.StockMapper;
import com.cloudticket.mapper.TicketMapper;
import com.cloudticket.pojo.dto.ShowAuditDTO;
import com.cloudticket.pojo.dto.ShowCategoryDTO;
import com.cloudticket.pojo.dto.ShowCreateDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.entity.*;
import com.cloudticket.pojo.enums.SessionStatus;
import com.cloudticket.pojo.enums.ShowStatus;
import com.cloudticket.pojo.vo.*;
import com.cloudticket.service.ShowService;
import com.cloudticket.service.VenueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowServiceImpl extends ServiceImpl<ShowInfoMapper, ShowInfo> implements ShowService {

    @Autowired
    ShowInfoMapper showInfoMapper;

    @Autowired
    SessionMapper sessionMapper;

    @Autowired
    TicketMapper ticketMapper;

    @Autowired
    StockMapper stockMapper;

    @Autowired
    AuditLogMapper auditLogMapper;

    @Autowired
    VenueService venueService;

    @Override
    public PageResult<ShowAdminVO> showAdminPage(ShowPageQuery showPageQuery) {
        Page<ShowAdminVO> page = new Page<>(showPageQuery.getPageNum(), showPageQuery.getPageSize());
        showInfoMapper.selectShowPage(page, showPageQuery.getStatus());
        List<ShowAdminVO> rows = page.getRecords().stream().map(vo -> {
            vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());   // 列表也要状态翻译
            return vo;
        }).toList();
        return new PageResult<>((int) page.getTotal(), rows);
    }

    @Override
    public ShowAuditDetailVO showAdminDetail(Long id) {
        // ① 演出 + 主办方名称：一条 SQL（LEFT JOIN merchant），直接映射 VO
        ShowAuditDetailVO vo = showInfoMapper.selectAuditDetail(id);
        if (vo == null) throw new BaseException("演出不存在");
        vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());

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
    public void showAudit(Long id, ShowAuditDTO showAuditDTO) {
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)   // 三张表一起写：任何一步失败全回滚
    public IdVO showcreate(ShowCreateDTO showCreateDTO) {
        validDate(showCreateDTO);                                                  // 有效性校验
        ShowInfo show = createShow(showCreateDTO, getTotalStock(showCreateDTO));   // 写 show_info
        insertShowStructure(show.getId(), showCreateDTO);                          // 写票档 + 场次 + 库存
        return new IdVO(show.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void showUpdate(Long id, ShowCreateDTO showCreateDTO) {
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
        ticketMapper.delete(new LambdaQueryWrapper<TicketCategory>().eq(TicketCategory::getShowId, id));
        sessionMapper.delete(new LambdaQueryWrapper<ShowSession>().eq(ShowSession::getShowId, id));

        // 更新 show_info（只更新，不新增；status 保持不变）
        BeanUtils.copyProperties(showCreateDTO, show);   // title/category/coverUrl/detail/guide/venueId
        show.setTotalStock(getTotalStock(showCreateDTO));
        updateById(show);

        // 重建子表（票档 + 场次 + 库存）
        insertShowStructure(id, showCreateDTO);
    }

    @Override
    public PageResult<ShowMerchantVO> showMerchantPage(ShowPageQuery showPageQuery) {
        Long currentId = BaseContext.getCurrentId();
        Integer status = showPageQuery.getStatus();
        Page<ShowInfo> page=new Page<>(showPageQuery.getPageNum(),showPageQuery.getPageSize());
        page=lambdaQuery()
                .eq(ShowInfo::getMerchantId,currentId)
                .eq(status!=null,ShowInfo::getStatus,status)
                .page(page);
        List<ShowMerchantVO> list = page.getRecords().stream().map(showInfo -> {
            ShowMerchantVO showMerchantVO = new ShowMerchantVO();
            BeanUtils.copyProperties(showInfo, showMerchantVO);
            showMerchantVO.setStatusDesc(ShowStatus.of(showInfo.getStatus()).getDesc());
            return showMerchantVO;
        }).toList();
        return new PageResult<>((int)page.getTotal(),list);
    }

    @Override
    public ShowMerchantDetailVO showMerchantDetail(Long id) {
        Long merchantId = BaseContext.getCurrentId();
        ShowMerchantDetailVO vo=showInfoMapper.selectMerchantDetail(id,merchantId);
        if (vo==null){
            throw new BaseException("该演出不存在");
        }
        vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());
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
                .eq(ShowInfo::getStatus,ShowStatus.ON_SALE.getCode())
                .eq(ShowInfo::getMerchantId,BaseContext.getCurrentId())
                .eq(ShowInfo::getId,id)
                .update();
        if(!success){
            throw new BaseException("该演出不存在或当前状态不可下架");
        }
    }

    private static int getTotalStock(ShowCreateDTO showCreateDTO) {
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

    private void validDate(ShowCreateDTO showCreateDTO) {
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
        for (ShowCategoryDTO category : showCreateDTO.getCategories()) {
            if (!categoryNames.add(category.getName())) {
                throw new BaseException("票档名不能重复：" + category.getName());
            }
        }

    }
    /** 创建演出主记录（草稿态），返回回填 id 的实体 */
    private ShowInfo createShow(ShowCreateDTO showCreateDTO, int totalStock) {
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
        List<TicketCategory> categories = ticketMapper.selectList(
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
    private void insertShowStructure(Long showId, ShowCreateDTO showCreateDTO) {
        // 票档定义（演出级，一份）
        List<TicketCategory> categories = showCreateDTO.getCategories().stream().map(c -> {
            TicketCategory tc = new TicketCategory();
            BeanUtils.copyProperties(c, tc);              // name/price/limitPerUser
            tc.setShowId(showId);
            return tc;
        }).toList();
        categories.forEach(ticketMapper::insert);

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
                ShowCategoryDTO source = showCreateDTO.getCategories().get(i);   // 同下标对应
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
