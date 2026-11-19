package com.cloudticket.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 票码工具：静态签名凭证串（P1）
 * <p>
 * 格式：ticketNo + "." + HMAC-SHA256(ticketNo, secret)
 * 特性：防伪造（无密钥算不出签名）、防篡改（改票号即验签失败）；
 * HMAC 是确定性摘要，同一票号同一密钥结果恒定，因此不落库，查询时现算。
 * <p>
 * P4 升级：动态码在此格式上加时间戳并校验 30s 时间窗（本工具方法可保留复用）。
 */
public class TicketCodeUtil {

    /** 票号与签名的分隔符 */
    private static final String SEPARATOR = ".";

    //私有化构造器
    private TicketCodeUtil() {
    }

    /**
     * 签发：生成票码凭证串（前端直接渲染成二维码）
     */
    public static String sign(String ticketNo, String secret) {
        String mac = SecureUtil.hmacSha256(secret.getBytes(StandardCharsets.UTF_8)).digestHex(ticketNo);
        return ticketNo + SEPARATOR + mac;
    }

    /**
     * 验签：通过返回裸票号，失败返回 null
     * 步骤：拆分 → 重算签名 → 常量时间比对（防时序攻击）
     */
    public static String verifyAndExtract(String code, String secret) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        int idx = code.lastIndexOf(SEPARATOR);
        if (idx <= 0 || idx == code.length() - 1) {
            return null;
        }
        String ticketNo = code.substring(0, idx);
        String sign = code.substring(idx + 1);
        String expect = SecureUtil.hmacSha256(secret.getBytes(StandardCharsets.UTF_8)).digestHex(ticketNo);
        boolean ok = MessageDigest.isEqual(
                expect.getBytes(StandardCharsets.UTF_8),
                sign.getBytes(StandardCharsets.UTF_8));
        return ok ? ticketNo : null;
    }
}
