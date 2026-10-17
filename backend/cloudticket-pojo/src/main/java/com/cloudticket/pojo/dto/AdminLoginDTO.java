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
public class AdminLoginDTO {

    @NotBlank(message = "账户名不可以为空")
    private String username;

    @NotBlank(message = "密码不可以为空")
    @Size(min = 6, max = 20, message = "密码应为6-20个字符")
    private String password;
}
