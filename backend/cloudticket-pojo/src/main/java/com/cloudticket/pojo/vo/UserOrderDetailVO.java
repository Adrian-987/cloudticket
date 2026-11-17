package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情（用户端，契约 04 §4.3）
 * items 按票档聚合（quantity = 票位行数，attendeeNames = 脱敏姓名列表）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderDetailVO {

    private Long orderId;

    private String orderNo;

    private String showTitle;

    private String sessionName;

    private LocalDateTime sessionTime;

    private BigDecimal amount;

    private BigDecimal payAmount;

    private Integer status;

    private String statusDesc;

    private LocalDateTime expireTime;

    private LocalDateTime payTime;

    private List<UserOrderItemVO> items;

    private List<UserTicketVO> tickets;
}
