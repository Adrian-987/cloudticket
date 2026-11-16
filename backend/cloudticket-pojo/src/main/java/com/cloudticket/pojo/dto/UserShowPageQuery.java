package com.cloudticket.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserShowPageQuery {
    private Integer pageNum=1;
    private Integer pageSize=10;
    private Integer category;
    private String city;
    private String keyword;
}
