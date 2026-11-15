package com.cloudticket.pojo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建/编辑演出：票档定义（演出级，一份）+ 场次列表。
 * 库存：每个票档的 stock/memberStock 为"每个场次的初始库存"，后端按 场次 × 票档 生成交叉库存。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 128, message = "标题需 2~128 字")
    private String title;

    @NotBlank(message = "分类不能为空")
    @Size(max = 50, message = "分类最多 50 字")
    private String category;

    @NotNull(message = "场馆不能为空")
    private Long venueId;

    @Size(max = 500, message = "封面地址过长")
    private String coverUrl;

    private String detail;

    private String guide;

    @NotEmpty(message = "至少需要一个票档")
    @Size(max = 5, message = "最多 5 个票档")
    @Valid
    private List<ShowCategoryDTO> categories;

    @NotEmpty(message = "至少需要一个场次")
    @Size(max = 10, message = "最多 10 个场次")
    @Valid
    private List<ShowSessionDTO> sessions;
}
