package com.cloudticket.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketCheckVO {
    private String ticketNo;
    private String showTitle;
    private String sessionName;
    private String categoryName;
    private String attendeeName;
    //审计专用,不返回前端
    @JsonIgnore
    private Long id;
}
