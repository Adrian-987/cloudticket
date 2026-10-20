package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowAuditDetailVO {
    private Long id;
    private String title;
    private String merchantName;
    private String category;
    private String detail;        // 富文本（按 06 的详情字段来）
    private String guide;
    private String coverUrl;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
    private List<SessionVO> sessions;
    private List<AuditRecordVO> auditRecords;
}

