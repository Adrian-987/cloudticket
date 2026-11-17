package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateOrderVO {
    private String orderNo;
    private Long orderId;
    private BigDecimal payAmount;
    private LocalDateTime expireTime;
}
