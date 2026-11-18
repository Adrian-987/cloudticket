package com.cloudticket.controller.user;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.UserTicketPageQuery;
import com.cloudticket.pojo.vo.UserTicketPageVO;
import com.cloudticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserTicketController {

    @Autowired
    TicketService ticketService;

    @GetMapping("/tickets/page")
    public Result<PageResult<UserTicketPageVO>> ticketUserPage(UserTicketPageQuery userTicketPageQuery){
        PageResult<UserTicketPageVO> pageResult=ticketService.ticketUserPage(userTicketPageQuery);
        return Result.success(pageResult);
    }
}
