package com.cloudticket.controller;

import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AttendeeSaveDTO;
import com.cloudticket.pojo.dto.UserLoginDTO;
import com.cloudticket.pojo.dto.UserRegisterDTO;
import com.cloudticket.pojo.vo.*;
import com.cloudticket.service.AttendeeService;
import com.cloudticket.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AttendeeService attendeeService;

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

    @PostMapping("/attendee")
    public Result<IdVO> attendeeSave(@RequestBody @Valid AttendeeSaveDTO attendeeSaveDTO){
        Long id=attendeeService.attendeeSave(attendeeSaveDTO);
        return Result.success(new IdVO(id));
    }

    @DeleteMapping("/attendee/{id}")
    public Result attendeeDelect(@PathVariable Long id){
        attendeeService.attendeeDelete(id);
        return Result.success();
    }

    @PutMapping("/attendee/{id}")
    public Result attendeeUpdate(@PathVariable Long id,@RequestBody @Valid AttendeeSaveDTO attendeeSaveDTO){
        attendeeService.attendeeUpdate(id,attendeeSaveDTO);
        return Result.success();
    }

    @GetMapping("/attendee/list")
    public Result<List<AttendeeVO>> attendeeList(){
        List<AttendeeVO> list=attendeeService.attendeeList();
        return Result.success(list);
    }
}
