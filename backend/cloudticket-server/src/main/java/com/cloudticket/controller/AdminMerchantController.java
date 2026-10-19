package com.cloudticket.controller;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AuditDTO;
import com.cloudticket.pojo.dto.MerchantPageQuery;
import com.cloudticket.pojo.vo.MerchantAuditVO;
import com.cloudticket.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/merchants/page")
    public Result<PageResult<MerchantAuditVO>> merchantAuditList(MerchantPageQuery merchantPageQuery){
        PageResult<MerchantAuditVO> pageResult=merchantService.merchantAuditList(merchantPageQuery);
        return Result.success(pageResult);
    }

    @PostMapping("/merchants/{id}/audit")
    public Result merchantAuditUpdate(@PathVariable Long id,@RequestBody AuditDTO auditDTO){
        merchantService.merchantAuditUpdate(id,auditDTO);
        return Result.success();
    }

    @PostMapping("/merchants/{id}/disable")
    public Result merchantDisable(@PathVariable Long id){
        merchantService.merchantDisable(id);
        return Result.success();
    }

    @PostMapping("/merchants/{id}/enable")
    public Result merchantEnable(@PathVariable Long id){
        merchantService.merchantEnable(id);
        return Result.success();
    }
}
