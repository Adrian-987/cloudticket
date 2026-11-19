package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.TicketCheckDTO;
import com.cloudticket.pojo.dto.MerchantTicketPageQuery;
import com.cloudticket.pojo.dto.UserTicketPageQuery;
import com.cloudticket.pojo.entity.Ticket;
import com.cloudticket.pojo.vo.MerchantTicketPageVO;
import com.cloudticket.pojo.vo.TicketCheckVO;
import com.cloudticket.pojo.vo.UserTicketPageVO;

public interface TicketService extends IService<Ticket> {
    PageResult<UserTicketPageVO> ticketUserPage(UserTicketPageQuery userTicketPageQuery);

    TicketCheckVO ticketCheck(TicketCheckDTO ticketCheckDTO);

    PageResult<MerchantTicketPageVO> ticketMerchantPage(MerchantTicketPageQuery ticketMerchantPageQuery);
}
