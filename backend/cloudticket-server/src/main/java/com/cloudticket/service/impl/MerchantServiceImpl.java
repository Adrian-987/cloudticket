package com.cloudticket.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.util.JwtUtil;
import com.cloudticket.common.util.PasswordUtil;
import com.cloudticket.config.JwtProperties;
import com.cloudticket.mapper.MerchantMapper;
import com.cloudticket.pojo.dto.MerchantLoginDTO;
import com.cloudticket.pojo.entity.Merchant;
import com.cloudticket.pojo.enums.MerchantStatus;
import com.cloudticket.pojo.vo.MerchantInfoVO;
import com.cloudticket.pojo.vo.MerchantLoginVO;
import com.cloudticket.service.MerchantService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public MerchantLoginVO merchantLogin(MerchantLoginDTO merchantLoginDTO) {
        Merchant one = lambdaQuery().eq(Merchant::getContactPhone, merchantLoginDTO.getPhone()).one();
        if(one==null){
            throw new BaseException("账号或密码错误");
        }
        boolean ok= PasswordUtil.matches(merchantLoginDTO.getPassword(),one.getPasswordHash());
        if(!ok){
            throw new BaseException("账号或密码错误");
        }
        if(MerchantStatus.of(one.getStatus())==MerchantStatus.DISABLED){
            throw new BaseException("账号已被禁用");
        } else if (MerchantStatus.of(one.getStatus())==MerchantStatus.PENDING_REVIEW) {
            throw new BaseException("入驻审核中，请等待平台确认");
        }
        Map<String,Object> claims=new HashMap<>();
        claims.put("merchantId",one.getId());
        String token= JwtUtil.createToken(claims,
                jwtProperties.getMerchant().getSecret(),
                jwtProperties.getMerchant().getTtlHours());
        return MerchantLoginVO.builder()
                .token(token)
                .merchantId(one.getId())
                .name(one.getName())
                .status(one.getStatus())
                .build();
    }

    @Override
    public MerchantInfoVO merchantInfo() {
        Long currentId = BaseContext.getCurrentId();
        Merchant merchant = getById(currentId);
        if(merchant==null){
            throw new BaseException("登录信息错误,请重新登录");
        }
        MerchantInfoVO merchantInfoVO=new MerchantInfoVO();
        BeanUtils.copyProperties(merchant,merchantInfoVO);
        merchantInfoVO.setMerchantId(merchant.getId());
        merchantInfoVO.setPhone(DesensitizedUtil.mobilePhone(merchant.getContactPhone()));
        merchantInfoVO.setStatusDesc(MerchantStatus.of(merchant.getStatus()).getDesc());
        return merchantInfoVO;
    }
}
