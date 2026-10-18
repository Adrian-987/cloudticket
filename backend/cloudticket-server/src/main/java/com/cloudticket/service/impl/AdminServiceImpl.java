package com.cloudticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.constant.BaseConstant;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.util.JwtUtil;
import com.cloudticket.common.util.PasswordUtil;
import com.cloudticket.config.JwtProperties;
import com.cloudticket.mapper.AdminMapper;
import com.cloudticket.pojo.dto.AdminLoginDTO;
import com.cloudticket.pojo.entity.Admin;
import com.cloudticket.pojo.enums.EnableStatus;
import com.cloudticket.pojo.vo.AdminInfoVO;
import com.cloudticket.pojo.vo.AdminLoginVO;
import com.cloudticket.service.AdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    JwtProperties jwtProperties;

    @Override
    public AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        Admin one = lambdaQuery().eq(Admin::getUsername, adminLoginDTO.getUsername()).one();
        if(one==null){
            throw new BaseException("账号或密码错误");
        }
        boolean ok= PasswordUtil.matches(adminLoginDTO.getPassword(),one.getPasswordHash());
        if(!ok){
            throw new BaseException("账号或密码错误");
        }
        if (EnableStatus.of(one.getStatus()) == EnableStatus.DISABLED) {
            throw new BaseException("账号已被禁用");
        }
        Map<String,Object> claims=new HashMap<>();
        claims.put("adminId",one.getId());
        String token= JwtUtil.createToken(claims,
                jwtProperties.getAdmin().getSecret(),
                jwtProperties.getAdmin().getTtlHours());
        return AdminLoginVO.builder()
                .token(token)
                .realName(one.getRealName())
                .adminId(one.getId())
                .build();
    }

    @Override
    public AdminInfoVO adminInfo() {
        Long currentId = BaseConstant.getCurrentId();
        Admin admin = getById(currentId);
        if(admin==null){
            throw new BaseException("登录信息错误,请重新登录");
        }
        AdminInfoVO adminInfoVO=new AdminInfoVO();
        BeanUtils.copyProperties(admin,adminInfoVO);
        adminInfoVO.setAdminId(admin.getId());
        adminInfoVO.setStatusDesc(EnableStatus.of(admin.getStatus()).getDesc());
        return adminInfoVO;
    }
}
