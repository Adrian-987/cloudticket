package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueVO {
    private Long id;
    private String name;
    private String city;
    private String address;
    private BigDecimal lng;//预留GEO经纬度
    private BigDecimal lat;
    private LocalDateTime createTime;
}
