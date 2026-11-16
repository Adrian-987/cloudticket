package com.cloudticket.controller.admin;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AdminShowAuditDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.vo.AdminShowVO;
import com.cloudticket.pojo.vo.AdminShowDetailVO;
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
    public Result<PageResult<AdminShowVO>> showPage(ShowPageQuery showPageQuery){
        PageResult<AdminShowVO> pageResult=showService.showAdminPage(showPageQuery);
        return Result.success(pageResult);
    }

    @GetMapping("/shows/{id}")
    public Result<AdminShowDetailVO> showDetail(@PathVariable Long id){
        AdminShowDetailVO showAuditDetailVO=showService.showAdminDetail(id);
        return Result.success(showAuditDetailVO);
    }

    @PostMapping("/shows/{id}/audit")
    public Result showAudit(@PathVariable Long id, @RequestBody @Valid AdminShowAuditDTO showAuditDTO){
        showService.showAudit(id, showAuditDTO);
        return Result.success();
    }
}
