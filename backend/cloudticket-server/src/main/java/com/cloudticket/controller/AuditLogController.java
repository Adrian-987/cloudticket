package com.cloudticket.controller;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AuditLogPageQuery;
import com.cloudticket.pojo.vo.AuditLogVO;
import com.cloudticket.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/audit-logs")
public class AuditLogController {
    @Autowired
    AuditLogService auditLogService;

    @GetMapping("/page")
    public Result<PageResult<AuditLogVO>> auditLogPage(AuditLogPageQuery query){
        PageResult<AuditLogVO> pageResult=auditLogService.auditLogPage(query);
        return Result.success(pageResult);
    }
}
