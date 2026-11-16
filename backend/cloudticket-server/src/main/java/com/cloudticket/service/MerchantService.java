package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.MerchantAuditDTO;
import com.cloudticket.pojo.dto.MerchantLoginDTO;
import com.cloudticket.pojo.dto.AdminMerchantPageQuery;
import com.cloudticket.pojo.entity.Merchant;
import com.cloudticket.pojo.vo.AdminMerchantAuditVO;
import com.cloudticket.pojo.vo.MerchantInfoVO;
import com.cloudticket.pojo.vo.MerchantLoginVO;
import jakarta.validation.Valid;

public interface MerchantService extends IService<Merchant> {
    MerchantLoginVO merchantLogin(@Valid MerchantLoginDTO merchantLoginDTO);

    MerchantInfoVO merchantInfo();

    PageResult<AdminMerchantAuditVO> merchantAuditList(AdminMerchantPageQuery merchantPageQuery);

    void merchantAuditUpdate(Long id, MerchantAuditDTO auditDTO);

    void merchantDisable(Long id);

    void merchantEnable(Long id);
}
