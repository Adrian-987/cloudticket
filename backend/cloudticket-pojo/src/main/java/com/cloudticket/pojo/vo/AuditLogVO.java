package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogVO {
    private String operatorName;
    private String operatorTypeDesc;
    private String action;
    private String targetType;
    private Long targetId;
    private Integer result;
    private String ip;
    private String traceId;
    private LocalDateTime createTime;
}
