package com.cloudticket.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderPageQuery {

    /** 游标：上一页最后一条的订单 id（首页不传） */
    private Long cursor;

    /** 每页条数（默认 10，上限 50） */
    private Integer limit = 10;

    private Integer status;
}
