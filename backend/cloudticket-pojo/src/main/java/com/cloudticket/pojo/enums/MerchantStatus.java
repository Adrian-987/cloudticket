package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 主办方状态（merchant.status）
 */
@Getter
@AllArgsConstructor
public enum MerchantStatus {

    PENDING_REVIEW(0, "待审核"),
    NORMAL(1, "正常"),
    DISABLED(2, "禁用");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static MerchantStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (MerchantStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
