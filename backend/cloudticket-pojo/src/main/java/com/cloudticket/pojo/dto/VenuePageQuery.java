package com.cloudticket.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenuePageQuery {
    private Integer pageNum=1;
    private Integer pageSize = 10;
    private String name;
    private String city;
}
