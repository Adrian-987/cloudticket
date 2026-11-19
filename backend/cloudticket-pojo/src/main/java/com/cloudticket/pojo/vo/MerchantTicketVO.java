package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 主办方端 · 订单下的票（契约 06 §8.3 tickets[]） */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantTicketVO {

    private String ticketNo;

    /** 观演人姓名（脱敏，票面快照） */
    private String attendeeName;

    /** 票档名 */
    private String categoryName;

    private Integer status;

    /** 有效 / 已核销 / 已退款 / 已作废 */
    private String statusDesc;

    /** 核销时间（未核销为 null） */
    private LocalDateTime checkTime;
}
