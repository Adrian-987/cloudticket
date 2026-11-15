package com.cloudticket.pojo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowCategoryDTO {
    @NotBlank(message = "票档名不能为空")
    @Size(max = 50, message = "票档名最多 50 字")
    private String name;

    @NotNull
    @DecimalMin(value = "0.01", message = "票价必须大于 0")
    private BigDecimal price;

    @NotNull
    @Min(value = 0, message = "库存不能为负")
    private Integer stock;

    @NotNull
    @Min(value = 0, message = "会员库存不能为负")
    private Integer memberStock;

    @NotNull
    @Min(value = 1, message = "限购至少 1 张")
    private Integer limitPerUser;
}