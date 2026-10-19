package com.cloudticket.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.annotation.Audit;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.util.JwtUtil;
import com.cloudticket.common.util.PasswordUtil;
import com.cloudticket.config.JwtProperties;
import com.cloudticket.mapper.MerchantMapper;
import com.cloudticket.pojo.dto.AuditDTO;
import com.cloudticket.pojo.dto.MerchantLoginDTO;
import com.cloudticket.pojo.dto.MerchantPageQuery;
import com.cloudticket.pojo.entity.Merchant;
import com.cloudticket.pojo.enums.MerchantStatus;
import com.cloudticket.pojo.vo.MerchantAuditVO;
import com.cloudticket.pojo.vo.MerchantInfoVO;
import com.cloudticket.pojo.vo.MerchantLoginVO;
import com.cloudticket.service.MerchantService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
        MerchantStatus status = MerchantStatus.of(one.getStatus());
        if (status != MerchantStatus.NORMAL) {
            if (status == MerchantStatus.PENDING_REVIEW) {
                throw new BaseException("入驻审核中，请等待平台确认");
            }
            if (status == MerchantStatus.DISABLED) {
                throw new BaseException("账号已被禁用");
            }
            if (status == MerchantStatus.REJECTED) {
                throw new BaseException("入驻申请已被驳回：" + one.getAuditRemark());  // 驳回原因回显
            }
            throw new BaseException("账号状态异常，无法登录");
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

    @Override
    public PageResult<MerchantAuditVO> merchantAuditList(MerchantPageQuery merchantPageQuery) {
        Page<Merchant> page=new Page<Merchant>(merchantPageQuery.getPageNum(), merchantPageQuery.getPageSize());
        Integer status = merchantPageQuery.getStatus();
        page=lambdaQuery()
                .eq(status != null,Merchant::getStatus, status)
                .orderByAsc(Merchant::getStatus)
                .orderByDesc(Merchant::getCreateTime)
                .page(page);
        List<MerchantAuditVO> list = page.getRecords().stream().map(merchant -> {
            MerchantAuditVO merchantAuditVO = new MerchantAuditVO();
            BeanUtils.copyProperties(merchant, merchantAuditVO);
            merchantAuditVO.setStatusDesc(MerchantStatus.of(merchant.getStatus()).getDesc());
            merchantAuditVO.setContactPhone(DesensitizedUtil.mobilePhone(merchant.getContactPhone()));
            return merchantAuditVO;
        }).toList();
        return new PageResult<>((int) page.getTotal(),list);
    }

    @Override
    @Audit(action = "MERCHANT_AUDIT",targetType = "MERCHANT")
    public void merchantAuditUpdate(Long id, AuditDTO auditDTO) {
        if(!auditDTO.getApproved()&& StrUtil.isBlank(auditDTO.getRemark())){
            throw new BaseException("请填写驳回理由");
        }
        boolean success = lambdaUpdate().set(Merchant::getStatus, auditDTO.getApproved() ? 1 : 3)
                .set(Merchant::getAuditRemark, auditDTO.getRemark())
                .eq(Merchant::getStatus, MerchantStatus.PENDING_REVIEW.getCode())
                .eq(Merchant::getId, id)
                .update();
        if (!success) {
            throw new BaseException("该主办方不存在或已审核");
        }
    }

    @Override
    @Audit(action = "MERCHANT_DISABLE",targetType = "MERCHANT")
    public void merchantDisable(Long id) {
        // ① 先 CAS（并发安全的唯一裁决点）
        boolean success = lambdaUpdate()
                .set(Merchant::getStatus, MerchantStatus.DISABLED.getCode())
                .eq(Merchant::getId, id)
                .eq(Merchant::getStatus, MerchantStatus.NORMAL.getCode())   // 只有"正常"能禁用
                .update();

        if (success) {
            return;   // 成功：零额外查询
        }

        // ② 失败了才查库，解释"为什么没成功"
        Merchant one = getById(id);
        if (one == null) {
            throw new BaseException("主办方不存在");
        }
        MerchantStatus status = MerchantStatus.of(one.getStatus());
        if (status == MerchantStatus.DISABLED) {
            throw new BaseException("该主办方已是禁用状态");
        }
        if (status == MerchantStatus.PENDING_REVIEW) {
            throw new BaseException("待审核账号不能禁用，请先完成入驻审核");
        }
        if (status == MerchantStatus.REJECTED) {
            throw new BaseException("已驳回账号不能禁用");
        }
        throw new BaseException("当前状态不可禁用");
    }

    @Override
    @Audit(action = "MERCHANT_ENABLE", targetType = "MERCHANT")
    public void merchantEnable(Long id) {
        boolean success = lambdaUpdate().set(Merchant::getStatus, MerchantStatus.NORMAL.getCode())
                .eq(Merchant::getId, id)
                .eq(Merchant::getStatus, MerchantStatus.DISABLED.getCode())
                .update();
        if(success){
            return;
        }
        Merchant one= getById(id);
        if (one == null) {
            throw new BaseException("主办方不存在");
        }
        MerchantStatus status = MerchantStatus.of(one.getStatus());
        if (status == MerchantStatus.NORMAL) {
            throw new BaseException("该主办方已是启用状态");
        }
        if (status == MerchantStatus.PENDING_REVIEW) {
            throw new BaseException("待审核账号不能启用，请先完成入驻审核");
        }
        if (status == MerchantStatus.REJECTED) {
            throw new BaseException("已驳回账号不能启用");
        }
        throw new BaseException("当前状态不可禁用");
    }


}
