package com.cloudticket.annotation;

import com.cloudticket.common.constant.AuditConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计注解：标注方法由 AuditAspect 自动写 audit_log。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /** 动作编码（见 AuditConstant：MERCHANT_AUDIT / SHOW_AUDIT / TICKET_VERIFY ...） */
    String action();

    /** 目标类型（MERCHANT / SHOW / USER / TICKET ...） */
    String targetType();

    /**
     * 操作人类型（AuditConstant.OPERATOR_USER / OPERATOR_MERCHANT / OPERATOR_ADMIN）。
     * 默认平台管理员；主办方端接口须显式指定 OPERATOR_MERCHANT，否则审计会记成平台操作。
     */
    int operatorType() default AuditConstant.OPERATOR_ADMIN;
}
