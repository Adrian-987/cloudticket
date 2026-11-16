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
public class ShowMerchantVO {
    private Long id;
    private String title;
    private Integer category;
    private String categoryDesc;
    private String coverUrl;
    private Integer status;
    private String statusDesc;
    private String auditRemark;    // ★ 驳回原因回显（主办方专属）
    private LocalDateTime createTime;
}
