package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型（orders.order_type）
 */
@Getter
@AllArgsConstructor
public enum OrderType {

    TICKET(1, "演出票"),
    VIP(2, "会员开通");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static OrderType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
