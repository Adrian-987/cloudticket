package com.cloudticket.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowAuditDTO {
    @NotNull(message = "请提交是否通过")
    private Boolean approved;
    private String remark;
}
