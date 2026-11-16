package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 演出详情（主办方视角，契约 06 §4.3）
 * 与平台审核详情（AdminShowDetailVO）的差异：
 *  - 有 venueName（场馆名）、auditRemark（驳回原因）、totalStock/soldCount（运营数据）
 *  - 无 merchantName（自己看自己的演出，不需要主办方名）
 * 子结构 SessionVO / CategoryStockVO / AuditRecordVO 复用平台端同一套。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantShowDetailVO {

    private Long id;

    private String title;

    private Integer category;         // 分类 code（1演唱会 2话剧 3音乐会 4脱口秀 5音乐节 6其他）
    private String categoryDesc;      // 分类文案

    private String coverUrl;          // 封面

    private String detail;            // 图文详情（富文本）

    private String guide;             // 观演指南

    private Long venueId;

    private String venueName;         // ★ 场馆名（主办方需要知道在哪办）

    private Integer status;           // 0草稿 1待审核 2驳回 3审核通过 4销售中 5已结束 6已下架

    private String statusDesc;

    private String auditRemark;       // ★ 最近一次审核意见（驳回原因，驳回态回显）

    private Integer totalStock;       // ★ 总库存（运营数据）

    private Integer soldCount;        // ★ 总销量（运营数据）

    private LocalDateTime createTime;

    /** 场次 + 该场次的票档库存（复用平台端的嵌套结构） */
    private List<SessionVO> sessions;

    /** 审核履历（复用平台端 AuditRecordVO） */
    private List<AuditRecordVO> auditRecords;
}
