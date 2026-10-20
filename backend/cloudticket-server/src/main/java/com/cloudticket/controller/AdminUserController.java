package com.cloudticket.controller;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.UserPageQuery;
import com.cloudticket.pojo.vo.UserAdminVO;
import com.cloudticket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    UserService userService;

    @GetMapping("/users/page")
    public Result<PageResult<UserAdminVO>> userPage(UserPageQuery userPageQuery){
        PageResult<UserAdminVO> pageResult=userService.userPage(userPageQuery);
        return Result.success(pageResult);
    }

    @PostMapping("/users/{id}/disable")
    public Result userUnable(@PathVariable Long id){
        userService.userUnable(id);
        return Result.success();
    }

    @PostMapping("/users/{id}/enable")
    public Result userEnable(@PathVariable Long id){
        userService.userEnable(id);
        return Result.success();
    }
}
