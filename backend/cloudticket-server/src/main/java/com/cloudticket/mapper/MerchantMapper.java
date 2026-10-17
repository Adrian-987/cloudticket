package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudticket.pojo.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}
