package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 演出分类（前端下拉：值 + 文案） */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVO {

    /** 分类 code（1演唱会 2话剧 3音乐会 4脱口秀 5音乐节 6其他） */
    private Integer code;

    /** 分类文案 */
    private String label;
}
