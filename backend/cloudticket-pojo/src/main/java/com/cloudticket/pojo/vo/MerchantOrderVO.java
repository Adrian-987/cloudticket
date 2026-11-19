package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 主办方端 · 订单管理分页行（契约 06 §7.3）
 * 数据权限：只返回当前主办方（merchant_id）名下的订单；买家信息脱敏。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantOrderVO {

    private Long orderId;

    private String orderNo;

    /** 演出标题 */
    private String showTitle;

    /** 买家昵称（脱敏） */
    private String buyerNickname;

    /** 买家手机尾号 4 位 */
    private String buyerPhone;

    /** 票数（该订单票位数） */
    private Integer quantity;

    /** 应付金额 */
    private BigDecimal payAmount;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;
}
