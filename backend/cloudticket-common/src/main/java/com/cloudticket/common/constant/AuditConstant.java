package com.cloudticket.common.constant;

/**
 * 审计常量：动作编码（action）/ 目标类型（targetType）/ 操作人类型（operatorType）/ 结果（result）。
 * 动作编码与 docs/api/07 契约保持一致。
 */
public class AuditConstant {

    private AuditConstant() {
    }

    // ===== 动作编码（action）=====
    /** 主办方入驻审核 */
    public static final String MERCHANT_AUDIT = "MERCHANT_AUDIT";
    /** 主办方禁用 */
    public static final String MERCHANT_DISABLE = "MERCHANT_DISABLE";
    /** 主办方启用 */
    public static final String MERCHANT_ENABLE = "MERCHANT_ENABLE";
    /** 演出审核 */
    public static final String SHOW_AUDIT = "SHOW_AUDIT";
    /** 用户禁用 */
    public static final String USER_DISABLE = "USER_DISABLE";
    /** 用户启用 */
    public static final String USER_ENABLE = "USER_ENABLE";
    /** 退款审核（P4） */
    public static final String REFUND_AUDIT = "REFUND_AUDIT";
    /** 票券核销（P3） */
    public static final String TICKET_VERIFY = "TICKET_VERIFY";

    // ===== 目标类型（targetType）=====
    public static final String TARGET_MERCHANT = "MERCHANT";
    public static final String TARGET_SHOW = "SHOW";
    public static final String TARGET_USER = "USER";
    public static final String TARGET_ORDER = "ORDER";
    public static final String TARGET_TICKET = "TICKET";
    public static final String TARGET_REFUND = "REFUND";

    // ===== 操作人类型（operatorType）=====
    public static final int OPERATOR_USER = 1;
    public static final int OPERATOR_MERCHANT = 2;
    public static final int OPERATOR_ADMIN = 3;

    // ===== 结果（result）=====
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;
}
