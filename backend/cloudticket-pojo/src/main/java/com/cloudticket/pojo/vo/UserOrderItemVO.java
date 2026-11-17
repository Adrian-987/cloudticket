package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** 订单明细（按票档聚合，契约 04 §4.3 items[]） */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderItemVO {

    /** 票档名 */
    private String categoryName;

    /** 成交单价（快照） */
    private BigDecimal unitPrice;

    /** 票位数（该档几张票） */
    private Integer quantity;

    /** 观演人姓名（脱敏，保留姓：张三 → 张*） */
    private List<String> attendeeNames;
}
