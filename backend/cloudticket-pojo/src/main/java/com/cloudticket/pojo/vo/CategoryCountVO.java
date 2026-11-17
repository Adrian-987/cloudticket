package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 票档维度计数（限购历史统计用） */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryCountVO {
    private Long categoryId;
    private Integer cnt;
}
