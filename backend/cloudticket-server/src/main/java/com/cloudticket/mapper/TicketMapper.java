package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudticket.pojo.entity.Ticket;
import com.cloudticket.pojo.vo.UserTicketPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    /** 票夹分页（票 + 场次 + 演出 + 票档，全一对一；用户维度 + 可选状态筛选） */
    IPage<UserTicketPageVO> selectUserTicketPage(Page<UserTicketPageVO> page,
                                                 @Param("userId") Long userId,
                                                 @Param("status") Integer status);
}
