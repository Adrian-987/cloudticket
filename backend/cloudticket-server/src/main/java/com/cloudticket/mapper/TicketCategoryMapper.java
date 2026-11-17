package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudticket.pojo.entity.TicketCategory;
import com.cloudticket.pojo.vo.CategoryRemainVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketCategoryMapper extends BaseMapper<TicketCategory> {
    List<CategoryRemainVO> selectUserStock(@Param("sessionId") Long sessionId,
                                           @Param("showId") Long showId);
}
