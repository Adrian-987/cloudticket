package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态（orders.status）
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    WAIT_PAY(0, "待支付"),
    PAID(1, "已支付"),
    ISSUED(2, "已出票"),
    CHECKED(3, "已核销"),
    FINISHED(4, "已完成"),
    CANCELLED(5, "已取消"),
    REFUNDING(6, "退款中"),
    REFUNDED(7, "已退款");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static OrderStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
