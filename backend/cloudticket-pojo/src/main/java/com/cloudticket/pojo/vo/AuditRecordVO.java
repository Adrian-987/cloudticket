package com.cloudticket.pojo.vo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 演出审核历史记录（审核详情的"审核履历"） */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditRecordVO {
    private String operatorName;      // 审核人（admin.real_name）
    private Boolean approved;         // true 通过 / false 驳回（从 audit_log.params 解析）
    private String remark;            // 审核意见（驳回原因）
    private LocalDateTime createTime; // 审核时间
}
