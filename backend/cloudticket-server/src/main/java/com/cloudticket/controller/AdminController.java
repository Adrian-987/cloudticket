package com.cloudticket.controller;

import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AdminLoginDTO;
import com.cloudticket.pojo.vo.AdminInfoVO;
import com.cloudticket.pojo.vo.AdminLoginVO;
import com.cloudticket.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @PostMapping("/auth/login")
    public Result<AdminLoginVO> adminLogin(@RequestBody @Valid AdminLoginDTO adminLoginDTO){
        AdminLoginVO adminLoginVO=adminService.adminLogin(adminLoginDTO);
        return Result.success(adminLoginVO);

    }

    @GetMapping("/auth/me")
    public Result<AdminInfoVO> adminInfo(){
        AdminInfoVO adminInfoVO=adminService.adminInfo();
        return Result.success(adminInfoVO);
    }
}
