package com.cloudticket.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台端 · VIP 订单分页行（契约 08 §3.3）
 * 只含 order_type=2（会员开通）的虚拟订单；普通票务订单走主办方端。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminVipOrderVO {

    private Long orderId;

    private String orderNo;

    private Long userId;

    /** 用户手机号（脱敏） */
    private String phone;

    private String nickname;

    /** 档位名（月卡 / 年卡） */
    private String planName;

    /** 时长（月） */
    private Integer months;

    /** 订单金额 */
    private BigDecimal amount;

    private Integer status;

    /** 0 待支付 / 4 已完成 / 5 已取消 */
    private String statusDesc;

    private LocalDateTime createTime;

    /** 支付时间（未支付为 null） */
    private LocalDateTime payTime;

    //中间字段
    @JsonIgnore
    private String vipPlanCode;
}
