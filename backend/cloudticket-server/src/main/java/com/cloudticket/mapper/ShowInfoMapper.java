package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudticket.pojo.dto.UserShowPageQuery;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShowInfoMapper extends BaseMapper<ShowInfo> {

    IPage<AdminShowVO> selectShowPage(Page<AdminShowVO> page, @Param("status") Integer status);

    AdminShowDetailVO selectAuditDetail(@Param("id") Long id);

    MerchantShowDetailVO selectMerchantDetail(@Param("id") Long id, @Param("merchantId") Long merchantId);

    /** 用户端演出分页（公开浏览：只查审核通过/销售中，含场馆与最低价） */
    IPage<UserShowVO> selectUserShowPage(Page<UserShowVO> page, @Param("q") UserShowPageQuery q);

    /** 用户端演出详情（静态：演出 + 场馆 + 价格区间；场次由 Service 另查） */
    UserShowDetailVO selectUserDetail(@Param("id") Long id);

    /** 开售流转③：有场次销售中 → 销售中（3→4） */
    int startSale();

    /** 开售流转④：所有场次都结束 → 已结束（4→5） */
    int endShow();

    List<UserShowVO> selectUserShowsByIds(List<Long> ids);
}
