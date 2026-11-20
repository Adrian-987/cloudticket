package com.cloudticket.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端 · 会员信息（契约 08 §1.3）
 * isVip 为后端计算值（vipLevel=1 且 vipExpireTime > now），不落库。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVipVO {

    /** 0 普通 / 1 会员 */
    private Integer vipLevel;

    /** 会员到期时间（非会员为 null） */
    private LocalDateTime vipExpireTime;

    /** 是否有效会员（vipLevel=1 且未过期） */
    private Boolean isVip;

    /** 可开通档位 */
    private List<VipPlanVO> plans;
}
