package com.cloudticket.controller;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.ShowAuditDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.vo.ShowAdminVO;
import com.cloudticket.pojo.vo.ShowAuditDetailVO;
import com.cloudticket.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminShowController {
    @Autowired
    ShowService showService;

    @GetMapping("/shows/page")
    public Result<PageResult<ShowAdminVO>> showPage(ShowPageQuery showPageQuery){
        PageResult<ShowAdminVO> pageResult=showService.showPage(showPageQuery);
        return Result.success(pageResult);
    }

    @GetMapping("/shows/{id}")
    public Result<ShowAuditDetailVO> showDetail(@PathVariable Long id){
        ShowAuditDetailVO showAuditDetailVO=showService.showDetail(id);
        return Result.success(showAuditDetailVO);
    }

    @PostMapping("/shows/{id}/audit")
    public Result showAudit(@PathVariable Long id, @RequestBody @Valid ShowAuditDTO showAuditDTO){
        showService.showAudit(id, showAuditDTO);
        return Result.success();
    }
}
