package com.cloudticket.service.impl;


import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.constant.AuditConstant;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.util.JwtUtil;
import com.cloudticket.common.util.PasswordUtil;
import com.cloudticket.config.JwtProperties;
import com.cloudticket.mapper.UserMapper;
import com.cloudticket.pojo.dto.UserLoginDTO;
import com.cloudticket.pojo.dto.UserPageQuery;
import com.cloudticket.pojo.dto.UserRegisterDTO;
import com.cloudticket.pojo.entity.Merchant;
import com.cloudticket.pojo.enums.EnableStatus;
import com.cloudticket.pojo.enums.MerchantStatus;
import com.cloudticket.pojo.vo.UserAdminVO;
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
import java.util.List;
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
        Long currentId = BaseContext.getCurrentId();
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

    @Override
    public PageResult<UserAdminVO> userPage(UserPageQuery userPageQuery) {
        Page<User> page=new Page<>(userPageQuery.getPageNum(), userPageQuery.getPageSize());
        String name = userPageQuery.getNickname();
        String phone = userPageQuery.getPhone();
        Integer status = userPageQuery.getStatus();
        page= lambdaQuery().eq(status != null, User::getStatus, status)
                .like(name != null, User::getNickname, name)
                .eq(phone != null, User::getPhone, phone)
                .page(page);
        List<UserAdminVO> list = page.getRecords().stream().map(user -> {
            UserAdminVO userAdminVO = new UserAdminVO();
            BeanUtils.copyProperties(user, userAdminVO);
            userAdminVO.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));
            userAdminVO.setStatusDesc(EnableStatus.of(user.getStatus()).getDesc());
            userAdminVO.setUserId(user.getId());
            return userAdminVO;
        }).toList();
        return new PageResult<>((int)page.getTotal(),list);

    }

    @Override
    @Audit(action = AuditConstant.USER_DISABLE, targetType = AuditConstant.TARGET_USER)
    public void userUnable(Long id) {
        // ① 先 CAS（并发安全的唯一裁决点）
        boolean success = lambdaUpdate()
                .set(User::getStatus, EnableStatus.DISABLED.getCode())
                .eq(User::getId, id)
                .eq(User::getStatus, EnableStatus.NORMAL.getCode())   // 只有"正常"能禁用
                .update();
        if (success) {
            return;   // 成功：零额外查询
        }
        // ② 失败了才查库，解释"为什么没成功"
        User one = getById(id);
        if (one == null) {
            throw new BaseException("该用户不存在");
        }
        EnableStatus status = EnableStatus.of(one.getStatus());
        if (status == EnableStatus.DISABLED) {
            throw new BaseException("该用户已是禁用状态");
        }
        throw new BaseException("当前状态不可禁用");
    }

    @Override
    @Audit(action = AuditConstant.USER_ENABLE, targetType = AuditConstant.TARGET_USER)
    public void userEnable(Long id) {
        boolean success = lambdaUpdate().set(User::getStatus, EnableStatus.NORMAL.getCode())
                .eq(User::getStatus, EnableStatus.DISABLED.getCode())
                .eq(User::getId, id)
                .update();
        if(success){
            return;
        }
        User user = getById(id);
        if(user==null){
            throw new BaseException("该用户不存在");
        }
        if (user.getStatus()==EnableStatus.NORMAL.getCode()){
            throw new BaseException("该用户已启用");
        }
        throw new BaseException("当前状态不可禁用");
    }

}
