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
public class CheckinResultBO {
    // ===== 校验用（不返回前端）=====
    private Long ticketId;            // 审计 targetId
    private Integer status;           // 状态校验
    private Long merchantId;          // 归属校验
    private LocalDateTime sessionTime;// 时间校验（当日）

    // ===== 返回用 =====
    private String ticketNo;
    private String showTitle;
    private String sessionName;
    private String categoryName;
    private String attendeeName;
}
