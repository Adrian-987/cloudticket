package com.cloudticket.service.impl;

import com.cloudticket.mapper.AuditLogMapper;
import com.cloudticket.pojo.entity.AuditLog;
import com.cloudticket.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    AuditLogMapper auditLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void save(AuditLog auditLog) {
        auditLogMapper.insert(auditLog);
    }
}
