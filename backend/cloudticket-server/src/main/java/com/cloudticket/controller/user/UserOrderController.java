package com.cloudticket.controller.user;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.OrderCreateDTO;
import com.cloudticket.pojo.dto.UserOrderPageQuery;
import com.cloudticket.pojo.dto.UserVipCreateDTO;
import com.cloudticket.pojo.vo.UserCreateOrderVO;
import com.cloudticket.pojo.vo.UserCreateVipVO;
import com.cloudticket.pojo.vo.UserOrderDetailVO;
import com.cloudticket.pojo.vo.UserOrderVO;
import com.cloudticket.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserOrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/orders")
    public Result<UserCreateOrderVO> orderCreate(@RequestBody @Valid OrderCreateDTO orderCreateDTO){
        UserCreateOrderVO userOrderVO=orderService.orderCreate(orderCreateDTO);
        return Result.success(userOrderVO);
    }

    @PostMapping("/pay/mock/{orderNo}")
    public Result orderPay(@PathVariable String orderNo){
        orderService.orderPay(orderNo);
        return Result.success();
    }

    @GetMapping("/orders/page")
    public Result<PageResult<UserOrderVO>> orderPage(UserOrderPageQuery userOrderPageQuery){
        PageResult<UserOrderVO> pageResult = orderService.orderUserPage(userOrderPageQuery);
        return Result.success(pageResult);
    }

    /** 订单详情（明细按档聚合 + 票列表） */
    @GetMapping("/orders/{id}")
    public Result<UserOrderDetailVO> orderDetail(@PathVariable Long id) {
        return Result.success(orderService.orderDetail(id));
    }

    @PostMapping("/orders/{id}/cancel")
    public Result orderCancel(@PathVariable Long id){
        orderService.orderCancel(id);
        return Result.success();
    }

    @PostMapping("/vip/orders")
    public Result<UserCreateVipVO> userVipCreate(@RequestBody UserVipCreateDTO userVipCreateDTO){
        UserCreateVipVO userCreateVipVO=orderService.userVipCreate(userVipCreateDTO);
        return Result.success(userCreateVipVO);
    }
}
