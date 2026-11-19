package com.cloudticket.controller.merchant;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.TicketCheckDTO;
import com.cloudticket.pojo.dto.MerchantTicketPageQuery;
import com.cloudticket.pojo.vo.MerchantTicketPageVO;
import com.cloudticket.pojo.vo.TicketCheckVO;
import com.cloudticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant")
public class MerchantTicketController {

    @Autowired
    TicketService ticketService;

    @PostMapping("/tickets/check")
    public Result<TicketCheckVO> ticketCheck(@RequestBody TicketCheckDTO ticketCheckDTO){
        TicketCheckVO ticketCheckVO=ticketService.ticketCheck(ticketCheckDTO);
        return Result.success(ticketCheckVO);
    }

    @GetMapping("/check-records/page")
    public Result<PageResult<MerchantTicketPageVO>> ticketPage(MerchantTicketPageQuery ticketMerchantPageQuery){
        PageResult<MerchantTicketPageVO> pageResult=ticketService.ticketMerchantPage(ticketMerchantPageQuery);
        return Result.success(pageResult);
    }
}
