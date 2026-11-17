package com.cloudticket.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单项入参（OrderCreateDTO.items[] 的元素）
 * 一个票档 + 一组观演人 id；每个观演人对应一张票（数量 = attendeeIds.size()）。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemCreateDTO {

    @NotNull(message = "票档不能为空")
    private Long categoryId;

    @NotEmpty(message = "请选择观演人")
    private List<Long> attendeeIds;
}
