package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudticket.pojo.entity.ShowSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionMapper extends BaseMapper<ShowSession> {

    /** 开售流转①：到开售时间 → 销售中（0→1） */
    int startSale();

    /** 开售流转②：演出时间已过 → 已结束（1→2） */
    int endSession();
}
