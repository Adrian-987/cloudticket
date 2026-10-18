package com.cloudticket.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页出参：total=总记录数，rows=当前页数据列表
 * 与接口文档 00 规范对齐；MP 分页结果用 of(page.getTotal(), page.getRecords()) 转换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer total;   // 总记录数（用 Integer：JacksonConfig 会把 Long 序列化成字符串，分页契约要求 number）
    private List<T> rows;    // 当前页数据列表

    public static <T> PageResult<T> of(long total, List<T> rows) {
        return PageResult.<T>builder().total((int) total).rows(rows).build();
    }
}
