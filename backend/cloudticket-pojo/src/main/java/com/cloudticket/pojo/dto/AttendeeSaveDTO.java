package com.cloudticket.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendeeSaveDTO {
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度需 2~50 位")
    private String name;

    @NotBlank(message = "证件号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCardNo;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;   // 非必须：注意正则里用 ^$| 允许空串（否则传 "" 会校验失败）

}
