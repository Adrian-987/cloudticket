package com.cloudticket.controller;

import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.MerchantLoginDTO;
import com.cloudticket.pojo.vo.MerchantInfoVO;
import com.cloudticket.pojo.vo.MerchantLoginVO;
import com.cloudticket.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/merchant")
@RestController
public class MerchantController {

    @Autowired
    MerchantService merchantService;
    @PostMapping("/auth/login")
    public Result<MerchantLoginVO> merchantLogin(@RequestBody @Valid MerchantLoginDTO merchantLoginDTO){
        MerchantLoginVO merchantLoginVO=merchantService.merchantLogin(merchantLoginDTO);
        return Result.success(merchantLoginVO);
    }

    @GetMapping("/auth/me")
    public Result<MerchantInfoVO> merchantInfo(){
        MerchantInfoVO merchantInfoVO=merchantService.merchantInfo();
        return Result.success(merchantInfoVO);
    }
}
