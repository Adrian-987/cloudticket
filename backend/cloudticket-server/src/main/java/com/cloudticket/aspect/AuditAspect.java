package com.cloudticket.aspect;

import cn.hutool.json.JSONUtil;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.constant.AuditConstant;
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
    private static final int OPERATOR_TYPE_ADMIN = AuditConstant.OPERATOR_ADMIN;

    @Autowired
    AuditLogService auditLogService;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint pjp, Audit audit) throws Throwable {
        int result = AuditConstant.RESULT_SUCCESS;
        Object returnValue = null;
        try {
            returnValue = pjp.proceed();   // 执行业务
            return returnValue;
        } catch (Throwable e) {
            result = AuditConstant.RESULT_FAIL;
            throw e;                       // 原样抛回，不影响业务语义
        } finally {
            try {
                saveLog(pjp, audit, result, returnValue);   // 写审计（失败只 warn，不干扰业务）
            } catch (Exception ex) {
                log.warn("审计日志写入失败", ex);
            }
        }
    }

    /** 组装 + 落库（AuditLogService 用 REQUIRES_NEW 独立事务，业务回滚也留痕） */
    private void saveLog(ProceedingJoinPoint pjp, Audit audit, int result, Object returnValue) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperatorId(BaseContext.getCurrentId());
        auditLog.setOperatorType(audit.operatorType());          // 由注解指定（默认平台管理员）
        auditLog.setAction(audit.action());
        auditLog.setTargetType(audit.targetType());
        auditLog.setTargetId(getTargetId(pjp, returnValue));
        auditLog.setParams(JSONUtil.toJsonStr(pjp.getArgs()));   // 参数快照（含敏感字段时需脱敏）
        auditLog.setResult(result);
        auditLog.setIp(getIp());
        auditLogService.save(auditLog);
    }

    /**
     * 取目标 id：
     * ① 优先：方法参数中第一个 Long（如 merchantAuditUpdate(Long id, DTO dto)）
     * ② 兜底：返回值对象若含 getId() 则取之（如核销返回 CheckinResultVO.ticketId）
     */
    private Long getTargetId(ProceedingJoinPoint pjp, Object returnValue) {
        for (Object arg : pjp.getArgs()) {
            if (arg instanceof Long id) {
                return id;
            }
        }
        if (returnValue != null) {
            try {
                Object v = returnValue.getClass().getMethod("getId").invoke(returnValue);
                if (v instanceof Long id) {
                    return id;
                }
            } catch (Exception ignored) {
                // 返回值没有 getId()：targetId 记 null，靠 params 留痕
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
