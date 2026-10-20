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
public class UserAdminVO {
    private Long userId;
    private String phone;
    private String nickname;
    private Integer vipLevel;
    private Integer points;
    private Integer orderCount=0;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
}
