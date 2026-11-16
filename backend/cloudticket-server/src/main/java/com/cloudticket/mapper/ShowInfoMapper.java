package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudticket.pojo.dto.UserShowPageQuery;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.vo.AdminShowVO;
import com.cloudticket.pojo.vo.AdminShowDetailVO;
import com.cloudticket.pojo.vo.MerchantShowDetailVO;
import com.cloudticket.pojo.vo.UserShowDetailVO;
import com.cloudticket.pojo.vo.UserShowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShowInfoMapper extends BaseMapper<ShowInfo> {

    IPage<AdminShowVO> selectShowPage(Page<AdminShowVO> page, @Param("status") Integer status);

    AdminShowDetailVO selectAuditDetail(@Param("id") Long id);

    MerchantShowDetailVO selectMerchantDetail(@Param("id") Long id, @Param("merchantId") Long merchantId);

    /** 用户端演出分页（公开浏览：只查审核通过/销售中，含场馆与最低价） */
    IPage<UserShowVO> selectUserShowPage(Page<UserShowVO> page, @Param("q") UserShowPageQuery q);

    /** 用户端演出详情（静态：演出 + 场馆 + 价格区间；场次由 Service 另查） */
    UserShowDetailVO selectUserDetail(@Param("id") Long id);
}
