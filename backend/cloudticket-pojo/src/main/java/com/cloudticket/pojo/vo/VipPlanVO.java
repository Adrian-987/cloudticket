package com.cloudticket.pojo.vo;

import com.cloudticket.pojo.enums.VipPlanStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/** 会员档位（契约 08 §1.3 plans[]，由 VipPlan 枚举翻译而来） */
@Getter
@Builder
@AllArgsConstructor
public class VipPlanVO {

    private final String planCode;
    private final String planName;
    private final Integer months;
    private final BigDecimal price;

    /** 预置档位（内部共享，只读） */
    public static final VipPlanVO MONTH = of(VipPlanStatus.MONTH);
    public static final VipPlanVO YEAR  = of(VipPlanStatus.YEAR);
    public static final List<VipPlanVO> ALL = List.of(MONTH, YEAR);

    private static VipPlanVO of(VipPlanStatus plan) {
        return VipPlanVO.builder()
                .planCode(plan.name())
                .planName(plan.getPlanName())
                .months(plan.getMonths())
                .price(plan.getPrice())
                .build();
    }
}
