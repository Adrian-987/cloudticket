package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudticket.pojo.dto.MerchantOrderPageQuery;
import com.cloudticket.pojo.dto.OrderVipPageQuery;
import com.cloudticket.pojo.entity.Orders;
import com.cloudticket.pojo.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    /** 我的订单分页（用户维度 + 可选状态筛选） */
    IPage<UserOrderVO> selectUserOrderPage(Page<UserOrderVO> page,
                                           @Param("userId") Long userId,
                                           @Param("status") Integer status);

    /** 订单详情主查询（订单 + 演出 + 场次，一对一；归属校验 user_id） */
    UserOrderDetailVO selectUserOrderDetail(@Param("orderId") Long orderId,
                                            @Param("userId") Long userId);

    /** 主办方订单分页（数据权限 merchant_id + 可选状态/演出/订单号） */
    IPage<MerchantOrderVO> selectMerchantOrderPage(Page<MerchantOrderVO> page,
                                                   @Param("merchantId") Long merchantId,
                                                   @Param("q") MerchantOrderPageQuery query);

    /** 主办方订单详情主查询（平铺 buyer 列 → resultMap association 映射） */
    MerchantOrderDetailVO selectMerchantOrder(@Param("id") Long id, @Param("merchantId") Long merchantId);

    /** 平台端 VIP 订单分页（返回 IPage，否则 MP 只填 total、rows 为空） */
    IPage<AdminVipOrderVO> selectAdminVip(Page<AdminVipOrderVO> page, @Param("q") OrderVipPageQuery q);
}
