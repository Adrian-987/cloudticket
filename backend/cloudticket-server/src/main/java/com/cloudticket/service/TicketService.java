package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.UserTicketPageQuery;
import com.cloudticket.pojo.entity.Ticket;
import com.cloudticket.pojo.vo.UserTicketPageVO;

public interface TicketService extends IService<Ticket> {
    PageResult<UserTicketPageVO> ticketUserPage(UserTicketPageQuery userTicketPageQuery);
}
