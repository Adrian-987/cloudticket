package com.cloudticket.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志表（audit_log）—— 只追加，无更新字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;

    private Integer operatorType;

    private String action;

    private String targetType;

    private Long targetId;

    private String params;

    private Integer result;

    private String ip;

    private String traceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
