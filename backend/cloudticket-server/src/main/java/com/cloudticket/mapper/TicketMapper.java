package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudticket.pojo.dto.MerchantTicketPageQuery;
import com.cloudticket.pojo.entity.Ticket;
import com.cloudticket.pojo.vo.CheckinResultBO;
import com.cloudticket.pojo.vo.MerchantTicketPageVO;
import com.cloudticket.pojo.vo.MerchantTicketVO;
import com.cloudticket.pojo.vo.UserTicketPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    /** 票夹分页（票 + 场次 + 演出 + 票档，全一对一；用户维度 + 可选状态筛选） */
    IPage<UserTicketPageVO> selectUserTicketPage(Page<UserTicketPageVO> page,
                                                 @Param("userId") Long userId,
                                                 @Param("status") Integer status);

    List<MerchantTicketVO> selectMerchant(@Param("id") Long id, @Param("currentId") Long currentId);

    CheckinResultBO selectByNO(@Param("ticketNo") String ticketNo);

    /** 核销记录分页（返回 List 才能让 MP 把结果写回 Page；void 会只有 total 没有 rows） */
    List<MerchantTicketPageVO> ticketMerchantPage(Page<MerchantTicketPageVO> page,
                                                  @Param("sessionId") Long sessionId,
                                                  @Param("merchantId") Long merchantId);
}
