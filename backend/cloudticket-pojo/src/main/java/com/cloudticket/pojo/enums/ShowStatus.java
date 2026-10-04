package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 演出状态（show_info.status）
 */
@Getter
@AllArgsConstructor
public enum ShowStatus {

    DRAFT(0, "草稿"),
    PENDING_REVIEW(1, "待审核"),
    REJECTED(2, "驳回"),
    APPROVED(3, "审核通过"),
    ON_SALE(4, "销售中"),
    ENDED(5, "已结束"),
    OFF_SALE(6, "已下架");

    private final int code;
    private final String desc;

    /**
     * 按数据库存储的数字反查枚举，未匹配返回 null
     */
    public static ShowStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ShowStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
