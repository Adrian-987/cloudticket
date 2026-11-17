package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户订单 VO（两处共用）：
 * 1) 下单返回（契约 04 §1.3）：orderId / orderNo / payAmount / expireTime（其余为 null）
 * 2) 我的订单分页 rows（契约 04 §3.3）：全部字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderVO {

    /** 订单 id */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 演出标题 */
    private String showTitle;

    /** 场次时间 */
    private LocalDateTime sessionTime;

    /** 应付金额 */
    private BigDecimal payAmount;

    /** 订单状态（0待支付 1已支付 2已出票 3已核销 4已完成 5已取消 6退款中 7已退款） */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 支付截止时间（待支付时前端做倒计时） */
    private LocalDateTime expireTime;

    /** 下单时间 */
    private LocalDateTime createTime;
}
