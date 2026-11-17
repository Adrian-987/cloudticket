package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudticket.pojo.entity.SessionCategoryStock;
import org.apache.ibatis.annotations.Param;

public interface StockMapper extends BaseMapper<SessionCategoryStock> {
    int saveCount(@Param("sessionId") Long sessionId,
                  @Param("categoryId") Long categoryId,
                  @Param("size") int size);

    int cancelCount(@Param("sessionId") Long sessionId,
                    @Param("categoryId") Long categoryId,
                    @Param("cnt") Integer cnt);
}
