package com.cloudticket.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueSaveDTO {
    @NotBlank(message = "场馆名字不可以为空")
    private String name;
    @NotBlank(message = "场馆城市不可以为空")
    private String city;
    @NotBlank(message = "场馆地址不可以为空")
    private String address;
    private BigDecimal lng;
    private BigDecimal lat;
}
