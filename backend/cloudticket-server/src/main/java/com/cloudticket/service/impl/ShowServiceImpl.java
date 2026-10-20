package com.cloudticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.constant.AuditConstant;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.AuditLogMapper;
import com.cloudticket.mapper.SessionMapper;
import com.cloudticket.mapper.ShowInfoMapper;
import com.cloudticket.mapper.StockMapper;
import com.cloudticket.mapper.TicketMapper;
import com.cloudticket.pojo.dto.ShowAuditDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.entity.SessionCategoryStock;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.entity.ShowSession;
import com.cloudticket.pojo.entity.TicketCategory;
import com.cloudticket.pojo.enums.SessionStatus;
import com.cloudticket.pojo.enums.ShowStatus;
import com.cloudticket.pojo.vo.*;
import com.cloudticket.service.ShowService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Override
    public PageResult<ShowAdminVO> showPage(ShowPageQuery showPageQuery) {
        Page<ShowAdminVO> page = new Page<>(showPageQuery.getPageNum(), showPageQuery.getPageSize());
        showInfoMapper.selectShowPage(page, showPageQuery.getStatus());
        List<ShowAdminVO> rows = page.getRecords().stream().map(vo -> {
            vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());   // 列表也要状态翻译
            return vo;
        }).toList();
        return new PageResult<>((int) page.getTotal(), rows);
    }

    @Override
    public ShowAuditDetailVO showDetail(Long id) {
        // ① 演出 + 主办方名称：一条 SQL（LEFT JOIN merchant），直接映射 VO
        ShowAuditDetailVO vo = showInfoMapper.selectAuditDetail(id);
        if (vo == null) throw new BaseException("演出不存在");
        vo.setStatusDesc(ShowStatus.of(vo.getStatus()).getDesc());

        // ② 场次：一次（按时间升序）
        List<ShowSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<ShowSession>()
                        .eq(ShowSession::getShowId, id)
                        .orderByAsc(ShowSession::getSessionTime));

        // ③ 票档定义：一次（演出级，全场次共用）
        List<TicketCategory> categories = ticketMapper.selectList(
                new LambdaQueryWrapper<TicketCategory>()
                        .eq(TicketCategory::getShowId, id)
                        .orderByAsc(TicketCategory::getId));

        // ④ 库存：一次批量（该演出所有场次的全部组合）
        //通过id集合查询库存集合,通过对应场次票档存储成map
        List<Long> sessionIds = sessions.stream().map(ShowSession::getId).toList();
        Map<String, SessionCategoryStock> stockMap = sessionIds.isEmpty() ? Map.of()
                : stockMapper.selectList(new LambdaQueryWrapper<SessionCategoryStock>()
                        .in(SessionCategoryStock::getSessionId, sessionIds))
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getSessionId() + ":" + s.getCategoryId(),  // 复合 key
                        s -> s));

        // ⑤ 笛卡尔积组装：场次 × 票档（纯内存）
        //2层循环,外层写场次信息,内层组成票档和库存
        List<SessionVO> sessionVOs = sessions.stream().map(s -> {
            SessionVO sv = new SessionVO();
            BeanUtils.copyProperties(s, sv);
            sv.setStatusDesc(SessionStatus.of(s.getStatus()).getDesc());
            List<CategoryStockVO> list = categories.stream().map(c -> {
                CategoryStockVO cv = new CategoryStockVO();
                BeanUtils.copyProperties(c,cv);
                cv.setCategoryId(c.getId());
                SessionCategoryStock st = stockMap.get(s.getId() + ":" + c.getId());
                if (st != null) {
                    BeanUtil.copyProperties(st, cv, true);   // Hutool 不支持 null source，缺行时保留 VO 默认值 0
                }
                return cv;
            }).toList();
            sv.setCategories(list);
            return sv;
        }).toList();
        vo.setSessions(sessionVOs);

        // ⑥ 审核历史：一条联表 SQL（admin 补名字 + JSON 函数直接解析 params）
        List<AuditRecordVO> auditRecords = auditLogMapper.selectShowAuditRecords(id);
        vo.setAuditRecords(auditRecords);
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

        // ③ CAS：只审"待审核"的演出（通过=3 / 驳回=2）
        boolean success = lambdaUpdate()
                .set(ShowInfo::getStatus, showAuditDTO.getApproved()
                        ? ShowStatus.APPROVED.getCode()
                        : ShowStatus.REJECTED.getCode())
                .eq(ShowInfo::getStatus, ShowStatus.PENDING_REVIEW.getCode())
                .eq(ShowInfo::getId, id)
                .update();
        if (!success) {
            throw new BaseException("该演出不存在或已审核");
        }
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

        Set<Long> sellableSessionIds = stocks.stream()
                .filter(s -> (s.getStock() != null && s.getStock() > 0)
                        || (s.getMemberStock() != null && s.getMemberStock() > 0))
                .map(SessionCategoryStock::getSessionId)
                .collect(Collectors.toSet());

        if (sellableSessionIds.size() < sessionIds.size()) {
            throw new BaseException("每个场次至少需要一个有库存的票档，不能通过审核");
        }
    }
}
