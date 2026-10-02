package com.cloudticket.aspect;

import com.cloudticket.annotation.AutoFill;
import com.cloudticket.annotation.OperationType;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 审计字段自动填充：拦截 mapper 包下标注 @AutoFill 的方法，
 * 反射调用实体的 setCreateTime/setCreateUser/setUpdateTime/setUpdateUser。
 * 填充失败必须抛异常触发事务回滚，绝不能吞掉——否则审计字段为 null，
 * 错误会推迟到 SQL 报错时才暴露，极难排查。
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.cloudticket.mapper.*.*(..)) && @annotation(com.cloudticket.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || args[0] == null) {
            return;
        }
        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        OperationType operationType = ((MethodSignature) joinPoint.getSignature())
                .getMethod().getAnnotation(AutoFill.class).value();
        try {
            if (operationType == OperationType.INSERT) {
                set(entity, "setCreateTime", LocalDateTime.class, now);
                set(entity, "setCreateUser", Long.class, currentId);
            }
            set(entity, "setUpdateTime", LocalDateTime.class, now);
            set(entity, "setUpdateUser", Long.class, currentId);
        } catch (Exception e) {
            log.error("审计字段填充失败, entity={}", entity.getClass().getName(), e);
            throw new BaseException("系统繁忙，请稍后重试");
        }
    }

    private void set(Object entity, String setter, Class<?> type, Object value) throws Exception {
        Method method = entity.getClass().getMethod(setter, type);
        method.invoke(entity, value);
    }
}
