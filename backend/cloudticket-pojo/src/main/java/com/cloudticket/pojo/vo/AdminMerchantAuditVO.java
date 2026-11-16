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
public class AdminMerchantAuditVO {
    private Long id;
    private String name;
    private String licenseNo;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;

}
