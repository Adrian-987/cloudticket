package com.cloudticket.service.impl;


import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.constant.BaseConstant;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.util.JwtUtil;
import com.cloudticket.common.util.PasswordUtil;
import com.cloudticket.config.JwtProperties;
import com.cloudticket.mapper.UserMapper;
import com.cloudticket.pojo.dto.UserLoginDTO;
import com.cloudticket.pojo.dto.UserRegisterDTO;
import com.cloudticket.pojo.enums.EnableStatus;
import com.cloudticket.pojo.vo.UserInfoVO;
import com.cloudticket.pojo.vo.UserLoginVO;
import com.cloudticket.pojo.vo.UserRegisterVO;
import com.cloudticket.pojo.entity.User;
import com.cloudticket.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    private  JwtProperties jwtProperties;

    @Override
    public UserRegisterVO userRegister(UserRegisterDTO userRegisterDTO) {
        String phone=userRegisterDTO.getPhone();
        User one = lambdaQuery().eq(User::getPhone, phone).one();
        if(one!=null){
            throw new BaseException("该手机号已注册");
        }
        if(StrUtil.isBlank(userRegisterDTO.getNickname())){
            String nickname="票友_"+phone.substring(phone.length()-4);
            userRegisterDTO.setNickname(nickname);
        }
        String hashCode= PasswordUtil.hash(userRegisterDTO.getPassword());
        User user=User.builder()
                .passwordHash(hashCode)
                .nickname(userRegisterDTO.getNickname())
                .phone(phone)
                .build();
        try {
            save(user);
        } catch (DuplicateKeyException e) {//接受指定的重复异常
            throw new BaseException("该手机号已注册");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());

        String token = JwtUtil.createToken(
                claims,
                jwtProperties.getUser().getSecret(),
                jwtProperties.getUser().getTtlHours());
        return UserRegisterVO.builder()
                .token(token)
                .nickname(userRegisterDTO.getNickname())
                .userId(user.getId())
                .build();
    }

    @Override
    public UserLoginVO userLogin(UserLoginDTO userLoginDTO) {
        User one = lambdaQuery().eq(User::getPhone, userLoginDTO.getPhone()).one();
        if(one==null){
            throw new BaseException("账号或密码错误");
        }
        boolean ok=PasswordUtil.matches(userLoginDTO.getPassword(),one.getPasswordHash());
        if(!ok){
            throw new BaseException("账号或密码错误");
        }
        if(EnableStatus.of(one.getStatus())==EnableStatus.DISABLED){
            throw new BaseException("该账号已被禁用");
        }
        Map<String,Object> claims=new HashMap<>();
        claims.put("userId",one.getId());
        String token=JwtUtil.createToken(claims,
                jwtProperties.getUser().getSecret(),
                jwtProperties.getUser().getTtlHours());
        return UserLoginVO.builder()
                .nickname(one.getNickname())
                .userId(one.getId())
                .token(token)
                .vipLevel(one.getVipLevel())
                .build();
    }

    @Override
    public UserInfoVO userInfo() {
        Long currentId = BaseConstant.getCurrentId();
        User user = getById(currentId);
        if(user==null){
            throw new BaseException("登录信息错误,请重新登录");
        }
        UserInfoVO userInfoVO=new UserInfoVO();
        BeanUtils.copyProperties(user,userInfoVO);
        userInfoVO.setUserId(user.getId());
        userInfoVO.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));
        return userInfoVO;
    }

}
