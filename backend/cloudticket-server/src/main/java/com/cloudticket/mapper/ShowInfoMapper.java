package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.vo.ShowAdminVO;
import com.cloudticket.pojo.vo.ShowAuditDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShowInfoMapper extends BaseMapper<ShowInfo> {

    IPage<ShowAdminVO> selectShowPage(Page<ShowAdminVO> page, @Param("status") Integer status);

    ShowAuditDetailVO selectAuditDetail(@Param("id") Long id);
}
