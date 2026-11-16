package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 演出详情（用户端静态内容，契约 03 §3.3）
 * 动态余量走独立接口（/user/sessions/{id}/categories），此处 sessions 仅骨架、不含票档。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserShowDetailVO {

    private Long id;

    private String title;

    private Integer category;      // 1~6

    private String categoryDesc;   // 分类文案

    private String coverUrl;

    private String detail;

    private String guide;

    private BigDecimal minPrice;   // 最低票档价（"xx 元起"）

    private BigDecimal maxPrice;   // 最高票档价

    private Integer status;        // 3审核通过 4销售中 5已结束 6已下架

    private String statusDesc;     // 用户端文案：即将开售/销售中/已结束/已下架

    private VenueVO venue;         // 场馆（一对一）

    private List<SessionSimpleVO> sessions;   // 场次骨架（不含票档库存）
}
