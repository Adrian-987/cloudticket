package com.cloudticket.pojo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 场次（不含票档——票档是演出级定义，见 MerchantShowSaveDTO.categories）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowSaveSessionDTO {

    @Size(max = 50, message = "场次名最多 50 字")
    private String sessionName;

    @NotNull(message = "演出开始时间不能为空")
    private LocalDateTime sessionTime;

    @NotNull(message = "开售时间不能为空")
    private LocalDateTime saleStartTime;

    @NotNull(message = "停售时间不能为空")
    private LocalDateTime saleEndTime;
}
