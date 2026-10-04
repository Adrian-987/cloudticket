package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存扣减池（orders.pool_type）
 */
@Getter
@AllArgsConstructor
public enum PoolType {

    PUBLIC(1, "公开池"),
    MEMBER(2, "会员池");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static PoolType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PoolType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
