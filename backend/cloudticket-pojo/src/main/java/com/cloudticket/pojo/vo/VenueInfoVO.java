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
public class VenueInfoVO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime createTime;
    private Integer showCount;
}
