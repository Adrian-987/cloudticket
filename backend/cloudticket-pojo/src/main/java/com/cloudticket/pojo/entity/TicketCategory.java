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
 * 票档定义表（ticket_category）—— 演出级，全场次通用
 * 票档是"座位等级"（内场VIP/看台A）：同一演出的所有场次共享这组定义（票价统一，改价即全改）。
 * 库存不在这里：场次 × 票档 的独立库存见 SessionCategoryStock。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("ticket_category")
public class TicketCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long showId;

    private String name;

    private BigDecimal price;

    private Integer limitPerUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
