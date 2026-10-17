package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {
    private Long userId;
    private String phone;
    private String nickname;
    private Integer vipLevel;
    private Integer points;
}
