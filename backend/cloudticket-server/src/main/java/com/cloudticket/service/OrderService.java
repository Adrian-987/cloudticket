package com.cloudticket.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.MerchantOrderPageQuery;
import com.cloudticket.pojo.dto.OrderCreateDTO;
import com.cloudticket.pojo.dto.UserOrderPageQuery;
import com.cloudticket.pojo.entity.Orders;
import com.cloudticket.pojo.vo.MerchantOrderDetailVO;
import com.cloudticket.pojo.vo.MerchantOrderVO;
import com.cloudticket.pojo.vo.UserCreateOrderVO;
import com.cloudticket.pojo.vo.UserOrderDetailVO;
import com.cloudticket.pojo.vo.UserOrderVO;
import jakarta.validation.Valid;

public interface OrderService extends IService<Orders> {
    UserCreateOrderVO orderCreate(@Valid OrderCreateDTO orderCreateDTO);

    void orderPay(String orderNo);

    PageResult<UserOrderVO> orderUserPage(UserOrderPageQuery userOrderPageQuery);

    /** 订单详情（明细按票档聚合 + 票列表） */
    UserOrderDetailVO orderDetail(Long orderId);

    /** 主办方订单分页（数据权限：只看自己名下） */
    PageResult<MerchantOrderVO> merchantOrderPage(MerchantOrderPageQuery query);

    void orderCancel(Long id);

    MerchantOrderDetailVO selectOrder(Long id);
}
