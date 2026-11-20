package com.cloudticket.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVipCreateDTO {

    /** 档位：MONTH / YEAR */
    @NotBlank(message = "请选择会员档位")
    private String planCode;
}
