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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 票表（ticket）—— 一票一码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("ticket")
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private Long orderId;

    private Long userId;

    private Long sessionId;

    private Long categoryId;

    /** 票价快照（出票时从 order_item.unit_price 复制，改价不影响历史票） */
    private BigDecimal price;

    private String attendeeName;

    private String idCardNo;

    private Integer status;

    private LocalDateTime checkTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
