package com.cloudticket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.TicketMapper;
import com.cloudticket.pojo.dto.UserTicketPageQuery;
import com.cloudticket.pojo.entity.Ticket;
import com.cloudticket.pojo.enums.TicketStatus;
import com.cloudticket.pojo.vo.UserTicketPageVO;
import com.cloudticket.service.TicketService;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {

    @Override
    public PageResult<UserTicketPageVO> ticketUserPage(UserTicketPageQuery userTicketPageQuery) {
        Page<UserTicketPageVO> page = new Page<>(userTicketPageQuery.getPageNum(), userTicketPageQuery.getPageSize());
        baseMapper.selectUserTicketPage(page, BaseContext.getCurrentId(), userTicketPageQuery.getStatus());
        page.getRecords().forEach(vo -> vo.setStatusDesc(TicketStatus.of(vo.getStatus()).getDesc()));
        return new PageResult<>((int) page.getTotal(), page.getRecords());
    }
}
