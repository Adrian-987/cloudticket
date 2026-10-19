package com.cloudticket.aspect;

import cn.hutool.json.JSONUtil;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.pojo.entity.AuditLog;
import com.cloudticket.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
  审计切面：拦截标注了 @Audit 的方法，把"谁、做了什么、对什么、参数、结果、IP"写入 audit_log。
 原则：审计是旁路——写库失败只 warn，绝不影响主业务；业务异常原样抛回，绝不吞掉。
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    /** 操作人类型：平台（P1 审计只用于管理端） **/
    private static final int OPERATOR_TYPE_ADMIN = 3;

    @Autowired
    AuditLogService auditLogService;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint pjp, Audit audit) throws Throwable {
        int result = 1;
        try {
            return pjp.proceed();          // 执行业务
        } catch (Throwable e) {
            result = 0;
            throw e;                       // 原样抛回，不影响业务语义
        } finally {
            try {
                saveLog(pjp, audit, result);   // 写审计（失败只 warn，不干扰业务）
            } catch (Exception ex) {
                log.warn("审计日志写入失败", ex);
            }
        }
    }

    /** 组装 + 落库（AuditLogService 用 REQUIRES_NEW 独立事务，业务回滚也留痕） */
    private void saveLog(ProceedingJoinPoint pjp, Audit audit, int result) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperatorId(BaseContext.getCurrentId());
        auditLog.setOperatorType(OPERATOR_TYPE_ADMIN);
        auditLog.setAction(audit.action());
        auditLog.setTargetType(audit.targetType());
        auditLog.setTargetId(getTargetId(pjp));
        auditLog.setParams(JSONUtil.toJsonStr(pjp.getArgs()));   // 参数快照（含敏感字段时需脱敏）
        auditLog.setResult(result);
        auditLog.setIp(getIp());
        auditLogService.save(auditLog);
    }

    /** 约定：方法参数中第一个 Long 为目标 id（如 merchantAuditUpdate(Long id, AuditDTO dto)） */
    private Long getTargetId(ProceedingJoinPoint pjp) {
        for (Object arg : pjp.getArgs()) {
            if (arg instanceof Long id) {
                return id;
            }
        }
        return null;
    }

    /** 取客户端 IP；非 Web 环境兜底 "-" */
    private String getIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs == null ? "-" : attrs.getRequest().getRemoteAddr();
        } catch (Exception e) {
            return "-";
        }
    }
}
