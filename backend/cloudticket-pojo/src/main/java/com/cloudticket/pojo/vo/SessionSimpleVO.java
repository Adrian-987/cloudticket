package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 场次骨架（用户端演出详情内嵌，不含票档库存）
 * 与 SessionVO 的区别：SessionVO 带 categories（管理端/主办方详情用）。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionSimpleVO {

    private Long id;

    private String sessionName;

    private LocalDateTime sessionTime;

    private LocalDateTime saleStartTime;

    private LocalDateTime saleEndTime;

    private Integer status;        // 0未开售 1销售中 2已结束

    private String statusDesc;
}
