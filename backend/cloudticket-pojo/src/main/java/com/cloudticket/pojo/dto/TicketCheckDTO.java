package com.cloudticket.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketCheckDTO {
    /** 票码凭证串（扫码得到或手动输入；格式 票号.HMAC签名） */
    private String ticketCode;
}
