package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 单个票档的余量（用户端选票面板，契约 03 §4.3 categories[]）
 * 注意：与 CategoryStockVO（管理端原始库存 stock/soldCount）语义不同——
 * 这里返回的是"剩余"（库存 - 已售），前端不需要自己算。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRemainVO {

    /** 票档 id（创建订单入参） */
    private Long id;

    private String name;

    private BigDecimal price;

    /** 公开池剩余 = stock - sold_count */
    private Integer remainStock;

    /** 会员池剩余 = member_stock - sold_member_count（真实值，是否展示由前端决定） */
    private Integer memberRemainStock;

    /** 单用户限购 */
    private Integer limitPerUser;
}
