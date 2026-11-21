package com.cloudticket.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 游标分页出参：rows=当前页数据，nextCursor=下一页游标（null=没有更多），hasMore=是否还有更多。
 * 适用：排序稳定（id DESC）的 C 端滚动列表（如"我的订单"）——不支持跳页、不返回总数。
 * 对比 PageResult（offset 分页）：深翻页时 offset 需扫过前 N 行，游标直接走主键索引。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> rows;

    /** 下一页游标（String 承载：项目 JacksonConfig 将 Long 序列化为字符串，避免前端精度问题） */
    private String nextCursor;

    private Boolean hasMore;
}
