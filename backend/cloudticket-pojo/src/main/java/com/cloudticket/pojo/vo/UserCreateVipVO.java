package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateVipVO {
    private Long orderId;
    private String orderNo;
    /** 支付截止时间（15 分钟） */
    private LocalDateTime expireTime;
    private BigDecimal payAmount;
}
