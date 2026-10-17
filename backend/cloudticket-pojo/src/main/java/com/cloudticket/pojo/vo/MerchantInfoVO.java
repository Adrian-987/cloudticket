package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantInfoVO {
    private Long merchantId;
    private String name;
    private String phone;
    private Integer status;
    private String statusDesc;
}
