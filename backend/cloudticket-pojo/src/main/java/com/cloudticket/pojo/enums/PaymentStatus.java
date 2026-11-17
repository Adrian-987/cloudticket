package com.cloudticket.pojo.enums;

import lombok.Getter;

/**
 * 支付状态（payment.status）：一笔支付尝试的结果
 * 0待支付 → 1成功 / 2失败；成功后退款 → 3已退款
 */
@Getter
public enum PaymentStatus {

    WAIT_PAY(0, "待支付"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String desc;

    PaymentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /** 按数据库存储的数字反查枚举，未匹配返回 null */
    public static PaymentStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
