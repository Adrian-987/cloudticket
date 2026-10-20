package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionVO {
    private Long id;
    private String sessionName;
    private LocalDateTime sessionTime;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private Integer status;          // 0未开售 1销售中 2已结束
    private String statusDesc;
    private List<CategoryStockVO> categories;
}
