package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 主办方端 · 订单详情（契约 06 §8.3）
 * 主办方视角：含买家信息（脱敏）与票列表（核销状态）。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantOrderDetailVO {

    private Long orderId;

    private String orderNo;

    private Long showId;

    private String showTitle;

    private String sessionName;

    /** 买家信息（脱敏） */
    private MerchantOrderBuyerVO buyer;

    /** 票数（来自 order_item 票位数，未支付时也有值） */
    private Integer quantity;

    private BigDecimal payAmount;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    /** 票列表（出票后才有数据） */
    private List<MerchantTicketVO> tickets;
}
