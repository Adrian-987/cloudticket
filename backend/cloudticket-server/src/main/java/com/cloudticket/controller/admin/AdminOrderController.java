package com.cloudticket.controller.admin;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.OrderVipPageQuery;
import com.cloudticket.pojo.vo.AdminVipOrderVO;
import com.cloudticket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminOrderController {

    @Autowired
    OrderService orderService;

    @GetMapping("/vip/orders/page")
    public Result<PageResult<AdminVipOrderVO>> vipOrderPage(OrderVipPageQuery orderVipPageQuery){
        PageResult<AdminVipOrderVO> pageResult=orderService.vipOrderPage(orderVipPageQuery);
        return Result.success(pageResult);
    }
}
