package com.cloudticket.pojo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建订单（用户端，契约 04 §1.2）
 * 约束：一次下单只能选一个场次，可跨多个票档，每个票档可含多个观演人（一人一票）。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateDTO {

    @NotNull(message = "场次不能为空")
    private Long sessionId;

    @NotEmpty(message = "请至少选择一个票档")
    @Size(max = 5, message = "一次最多选择 5 个票档")
    @Valid   // 嵌套集合必须加，否则子 DTO 的校验注解不生效
    private List<OrderItemCreateDTO> items;
}
