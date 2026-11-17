package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 订单下的票（契约 04 §4.3 tickets[]） */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTicketVO {

    private String ticketNo;

    private Integer status;

    /** 有效 / 已核销 / 已退款 / 已作废 */
    private String statusDesc;
}
