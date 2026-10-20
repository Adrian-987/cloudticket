package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowAdminVO {
    private Long id;
    private String title;
    private String category;
    private String merchantName;
    private String statusDesc;
    private Integer status;
    private LocalDateTime submitTime;
}
