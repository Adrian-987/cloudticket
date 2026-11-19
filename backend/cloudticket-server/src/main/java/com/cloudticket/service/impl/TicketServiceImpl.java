package com.cloudticket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.constant.AuditConstant;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.TicketMapper;
import com.cloudticket.pojo.dto.TicketCheckDTO;
import com.cloudticket.pojo.dto.MerchantTicketPageQuery;
import com.cloudticket.pojo.dto.UserTicketPageQuery;
import com.cloudticket.pojo.entity.Ticket;
import com.cloudticket.pojo.enums.TicketStatus;
import com.cloudticket.pojo.vo.*;
import com.cloudticket.service.TicketService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {

    @Autowired
    TicketMapper ticketMapper;

    @Override
    public PageResult<UserTicketPageVO> ticketUserPage(UserTicketPageQuery userTicketPageQuery) {
        Page<UserTicketPageVO> page = new Page<>(userTicketPageQuery.getPageNum(), userTicketPageQuery.getPageSize());
        baseMapper.selectUserTicketPage(page, BaseContext.getCurrentId(), userTicketPageQuery.getStatus());
        page.getRecords().forEach(vo -> vo.setStatusDesc(TicketStatus.of(vo.getStatus()).getDesc()));
        return new PageResult<>((int) page.getTotal(), page.getRecords());
    }

    @Override
    @Audit(action = AuditConstant.TICKET_VERIFY,
            targetType = AuditConstant.TARGET_TICKET,
            operatorType = AuditConstant.OPERATOR_MERCHANT)//审计字段
    public TicketCheckVO ticketCheck(TicketCheckDTO ticketCheckDTO) {
        //1.校验是否又no传递
        if (StrUtil.isBlank(ticketCheckDTO.getTicketNo())){
            throw new BaseException("请输入票号用于查询");
        }
        CheckinResultBO checkinResultBO=ticketMapper.selectByNO(ticketCheckDTO.getTicketNo());
        //2.校验数据是够存在
        if (checkinResultBO==null){
            throw new BaseException("该票号不存在,请核对后再次尝试");
        }
        //3.校验主办方
        if(!checkinResultBO.getMerchantId().equals(BaseContext.getCurrentId())){
            throw new BaseException("该票的演出不属于本主办方");
        }
        //4.校验核销时间
        if (!LocalDate.now().equals(checkinResultBO.getSessionTime().toLocalDate())){
            throw new BaseException("仅演出当日可核销");
        }
        //5.校验票状态（先给具体原因，再统一兜底）
        Integer status = checkinResultBO.getStatus();
        if (status == TicketStatus.CHECKED.getCode()) {
            throw new BaseException("该票已核销");
        }
        if (status == TicketStatus.REFUNDED.getCode()) {
            throw new BaseException("该票已退款");
        }
        if (status == TicketStatus.VOID.getCode()) {
            throw new BaseException("该票已作废");
        }
        if (status != TicketStatus.VALID.getCode()) {
            throw new BaseException("该票状态不可核销");
        }
        LocalDateTime now = LocalDateTime.now();
        Boolean ok =lambdaUpdate().set(Ticket::getCheckTime,now)
                .set(Ticket::getStatus,TicketStatus.CHECKED.getCode())
                .eq(Ticket::getStatus,TicketStatus.VALID.getCode())
                .eq(Ticket::getTicketNo,ticketCheckDTO.getTicketNo()).update();
        if (!ok){
            throw new BaseException("该卷状态不可核销,请重新尝试");
        }
        TicketCheckVO ticketCheckVO=new TicketCheckVO();
        BeanUtils.copyProperties(checkinResultBO,ticketCheckVO);
        ticketCheckVO.setId(checkinResultBO.getTicketId());
        return ticketCheckVO;
    }

    @Override
    public PageResult<MerchantTicketPageVO> ticketMerchantPage(MerchantTicketPageQuery ticketMerchantPageQuery) {
        Page<MerchantTicketPageVO> page = new Page<>(ticketMerchantPageQuery.getPageNum(), ticketMerchantPageQuery.getPageSize());
        List<MerchantTicketPageVO> rows = ticketMapper.ticketMerchantPage(page,
                ticketMerchantPageQuery.getSessionId(), BaseContext.getCurrentId());
        return new PageResult<>((int) page.getTotal(), rows);
    }
}
