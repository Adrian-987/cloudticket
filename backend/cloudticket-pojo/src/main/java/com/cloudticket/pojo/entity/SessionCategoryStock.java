package com.cloudticket.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 场次票档库存表（session_category_stock）—— 库存主体
 * 场次 × 票档 = 一个独立库存单元（笛卡尔积）；uk_session_category 联合唯一保证不重复。
 * version 配合 MP 乐观锁插件：UPDATE 自动拼 version=version+1 WHERE version=旧值。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("session_category_stock")
public class SessionCategoryStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long categoryId;

    private Integer stock;

    private Integer soldCount;

    private Integer memberStock;

    private Integer soldMemberCount;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
