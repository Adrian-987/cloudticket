package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 票夹分页行（用户端「我的票」，契约 05 §1.3）
 * 与 UserTicketVO（订单详情 tickets[]，仅票号/状态）区分：这里是完整的票卡片。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTicketPageVO {

    private Long ticketId;

    /** 票号（P1 明文占位；P4 换动态票码） */
    private String ticketNo;

    private String showTitle;

    private String sessionName;

    private LocalDateTime sessionTime;

    private String categoryName;

    private BigDecimal price;

    private Integer status;

    /** 有效 / 已核销 / 已退款 / 已作废 */
    private String statusDesc;

    /** 核销时间（未核销为 null） */
    private LocalDateTime checkTime;
}
