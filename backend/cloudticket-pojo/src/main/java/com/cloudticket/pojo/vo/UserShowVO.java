package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserShowVO {
    private Long id;
    private String title;
    private Integer category;
    private String categoryDesc;
    private String statusDesc;
    private String coverUrl;
    private String venueName;
    private String city;
    private BigDecimal minPrice;
    private LocalDateTime saleStartTime;   // 下一次开售时间（无未开售场次为 null）
    private Integer status;
}
