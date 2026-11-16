package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 场次票档余量（用户端选票面板，契约 03 §4.3）
 * 与 SessionSimpleVO 的区别：这里是"某一场次 + 各票档剩余"，不是场次骨架。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionStockVO {

    private Long sessionId;

    private Integer status;        // 场次状态：0未开售 1销售中 2已结束（前端据 status 置灰面板）

    private String statusDesc;

    private List<CategoryRemainVO> categories;
}
