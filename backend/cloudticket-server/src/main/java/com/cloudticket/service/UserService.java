package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.UserLoginDTO;
import com.cloudticket.pojo.dto.AdminUserPageQuery;
import com.cloudticket.pojo.dto.UserRegisterDTO;
import com.cloudticket.pojo.vo.AdminUserVO;
import com.cloudticket.pojo.vo.UserInfoVO;
import com.cloudticket.pojo.vo.UserLoginVO;
import com.cloudticket.pojo.vo.UserRegisterVO;
import com.cloudticket.pojo.entity.User;


public interface UserService extends IService<User> {

    UserRegisterVO userRegister(UserRegisterDTO userRegisterDTO);

    UserLoginVO userLogin(UserLoginDTO userLoginDTO);

    UserInfoVO userInfo();

    PageResult<AdminUserVO> userPage(AdminUserPageQuery userPageQuery);

    void userUnable(Long id);

    void userEnable(Long id);
}
