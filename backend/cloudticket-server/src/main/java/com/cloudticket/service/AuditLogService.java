package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.pojo.entity.AuditLog;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface AuditLogService  {
    @Transactional(propagation = Propagation.REQUIRES_NEW)   // 独立事务：业务回滚不影响审计留痕
    void save(AuditLog auditLog);
}
