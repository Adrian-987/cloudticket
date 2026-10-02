package com.cloudticket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要自动填充审计字段的 Mapper 方法：
 * INSERT 填 create_time/create_user/update_time/update_user，
 * UPDATE 只填 update_time/update_user。
 * 方法第一个参数必须是实体对象。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    OperationType value();
}
