package com.cloudticket.controller.admin;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AuditLogPageQuery;
import com.cloudticket.pojo.vo.AdminAuditLogVO;
import com.cloudticket.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/audit-logs")
public class AdminAuditLogController {
    @Autowired
    AuditLogService auditLogService;

    @GetMapping("/page")
    public Result<PageResult<AdminAuditLogVO>> auditLogPage(AuditLogPageQuery query){
        PageResult<AdminAuditLogVO> pageResult=auditLogService.auditLogPage(query);
        return Result.success(pageResult);
    }
}
