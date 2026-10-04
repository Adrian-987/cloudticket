package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用启用/禁用状态（user.status、admin.status）
 */
//枚举类在创造时new静态实例字段,用于业务引用,并创造静态方法,数字反推枚举类
@Getter
@AllArgsConstructor
public enum EnableStatus {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static EnableStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (EnableStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
