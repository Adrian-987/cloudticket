package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 场次状态（show_session.status）
 */
@Getter
@AllArgsConstructor
public enum SessionStatus {

    NOT_ON_SALE(0, "未开售"),
    ON_SALE(1, "销售中"),
    ENDED(2, "已结束");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static SessionStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (SessionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
