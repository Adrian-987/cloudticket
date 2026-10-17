package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.pojo.dto.MerchantLoginDTO;
import com.cloudticket.pojo.entity.Merchant;
import com.cloudticket.pojo.vo.MerchantInfoVO;
import com.cloudticket.pojo.vo.MerchantLoginVO;
import jakarta.validation.Valid;

public interface MerchantService extends IService<Merchant> {
    MerchantLoginVO merchantLogin(@Valid MerchantLoginDTO merchantLoginDTO);

    MerchantInfoVO merchantInfo();
}
