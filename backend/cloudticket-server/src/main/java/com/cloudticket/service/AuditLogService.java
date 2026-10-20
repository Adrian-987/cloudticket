package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.AuditLogPageQuery;
import com.cloudticket.pojo.entity.AuditLog;
import com.cloudticket.pojo.vo.AuditLogVO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface AuditLogService extends IService<AuditLog> {
    @Transactional(propagation = Propagation.REQUIRES_NEW)   // 独立事务：业务回滚不影响审计留痕
    boolean save(AuditLog auditLog);

    PageResult<AuditLogVO> auditLogPage(AuditLogPageQuery query);
}
