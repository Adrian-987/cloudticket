package com.cloudticket.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：签发与解析 token（无状态，密钥由调用方传入）
 */
public class JwtUtil {

    private JwtUtil() {}

    // 签发 token：claims 存身份信息，ttlHours 为有效期（小时）
    public static String createToken(Map<String, Object> claims, String secret, long ttlHours) {
        Date now = new Date();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));   // 将字符串规范化为 HMAC 密钥对象
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlHours * 3600_000L))
                .signWith(key)
                .compact();
    }

    // 解析 token：验签 + 过期检查，失败抛异常，成功返回 claims
    public static Claims parse(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)            // 用同一把密钥校验签名
                .build()
                .parseSignedClaims(token)   // 验签+过期检查（失败抛 ExpiredJwtException 等）
                .getPayload();              // 取出 claims
    }
}
