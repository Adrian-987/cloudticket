package com.cloudticket.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Aspect
public class ApiCostAspect {

    @Around("execution(* com.cloudticket.controller..*.*(..))")   // 拦截所有 Controller 方法
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();                 // 执行原方法
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost > 200) {
                log.warn("慢接口 {} 耗时 {}ms", pjp.getSignature().toShortString(), cost);
            }
        }
    }
}
