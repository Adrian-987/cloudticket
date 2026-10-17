package com.cloudticket.controller;

import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.UserLoginDTO;
import com.cloudticket.pojo.dto.UserRegisterDTO;
import com.cloudticket.pojo.vo.UserInfoVO;
import com.cloudticket.pojo.vo.UserLoginVO;
import com.cloudticket.pojo.vo.UserRegisterVO;
import com.cloudticket.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/auth/register")
    public Result<UserRegisterVO> userRegister(@RequestBody @Valid UserRegisterDTO userRegisterDTO){
        UserRegisterVO userRegisterVO=userService.userRegister(userRegisterDTO);
        return Result.success(userRegisterVO);
    }

    @PostMapping("/auth/login")
    public Result<UserLoginVO> userLogin(@RequestBody @Valid UserLoginDTO userLoginDTO){
        UserLoginVO userLoginVO=userService.userLogin(userLoginDTO);
        return Result.success(userLoginVO);
    }

    @GetMapping("/auth/me")
    public Result<UserInfoVO> userInfo(){
        UserInfoVO userInfoVO=userService.userInfo();
        return Result.success(userInfoVO);
    }
}
