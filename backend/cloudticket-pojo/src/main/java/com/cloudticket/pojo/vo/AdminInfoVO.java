package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminInfoVO {
    private Long adminId;
    private String username;
    private String realName;
    private Integer status;
    private String statusDesc;
}
