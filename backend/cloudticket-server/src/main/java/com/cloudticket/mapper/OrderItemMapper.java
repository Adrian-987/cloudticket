package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudticket.pojo.entity.OrderItem;
import com.cloudticket.pojo.vo.CategoryCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /** 实名判重：这些证件号在本场次已购记录数（排除取消/退款） */
    Long selectCounts(@Param("sessionId") Long sessionId, @Param("idCards") List<String> idCards);

    /** 限购：该用户在本场次各票档的历史已购数 */
    List<CategoryCountVO> selectHistoryCount(@Param("sessionId") Long sessionId,
                                             @Param("currentId") Long currentId,
                                             @Param("categoryIds") List<Long> categoryIds);

    /** 取消订单回补：该订单每个票档的票位数 */
    List<CategoryCountVO> selectCount1(@Param("orderId") Long orderId);
}
