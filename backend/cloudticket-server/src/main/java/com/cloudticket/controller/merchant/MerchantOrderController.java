package com.cloudticket.controller.merchant;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.MerchantOrderPageQuery;
import com.cloudticket.pojo.vo.MerchantOrderDetailVO;
import com.cloudticket.pojo.vo.MerchantOrderVO;
import com.cloudticket.service.OrderService;
import org.apache.ibatis.annotations.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant")
public class MerchantOrderController {

    @Autowired
    OrderService orderService;

    /** 订单管理分页（数据权限：只看自己名下订单） */
    @GetMapping("/orders/page")
    public Result<PageResult<MerchantOrderVO>> orderPage(MerchantOrderPageQuery query) {
        PageResult<MerchantOrderVO> data = orderService.merchantOrderPage(query);
        return Result.success(data);
    }

    @GetMapping("/orders/{id}")
    public Result<MerchantOrderDetailVO> selectOrder(@PathVariable Long id){
        MerchantOrderDetailVO merchantOrderDetailVO=orderService.selectOrder(id);
        return Result.success(merchantOrderDetailVO);
    }
}
