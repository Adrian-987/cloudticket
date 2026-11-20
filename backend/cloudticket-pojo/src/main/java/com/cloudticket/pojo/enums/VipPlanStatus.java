package com.cloudticket.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
@Getter
public enum VipPlanStatus {
    MONTH("月卡", 1,  new BigDecimal("19.90")),
    YEAR ("年卡", 12, new BigDecimal("199.00"));

    private final String planName;
    private final int months;
    private final BigDecimal price;

    public static VipPlanStatus of(String planCode) {
        if (planCode== null) {
            return null;
        }
        for (VipPlanStatus status : values()) {
            if (Objects.equals(status.name(), planCode)) {
                return status;
            }
        }
        return null;
    }
}
