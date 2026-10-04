package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 票状态（ticket.status），注意取值从 1 开始
 */
@Getter
@AllArgsConstructor
public enum TicketStatus {

    VALID(1, "有效"),
    CHECKED(2, "已核销"),
    REFUNDED(3, "已退款"),
    VOID(4, "已作废");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static TicketStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TicketStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
