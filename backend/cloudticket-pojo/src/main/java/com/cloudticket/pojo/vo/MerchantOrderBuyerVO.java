package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 主办方端 · 订单买家信息（脱敏，契约 06 §8.3 buyer） */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantOrderBuyerVO {

    /** 昵称（脱敏，如 小***） */
    private String nickname;

    /** 手机尾号 4 位 */
    private String phoneTail;
}
