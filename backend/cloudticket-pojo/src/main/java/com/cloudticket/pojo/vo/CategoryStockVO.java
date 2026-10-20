package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 票档 + 库存 的组合视图
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStockVO {
    private Long categoryId;         // ★ 票档定义 id（前端选票、下单入参用这个）
    private String name;             // ticket_category
    private BigDecimal price;        // ticket_category（全场次统一）
    private Integer limitPerUser;    // ticket_category
    private Integer stock=0;           // session_category_stock（该场次）
    private Integer soldCount=0;
    private Integer memberStock=0;
    private Integer soldMemberCount=0;
}
