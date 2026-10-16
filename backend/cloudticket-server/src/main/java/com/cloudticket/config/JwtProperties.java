package com.cloudticket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//类用于储存jwt的私密信息,通过内部类分类储存,并注入ioc容器
@Data
@Component
//读取yml文件,对应字段注入内部类
@ConfigurationProperties(prefix = "cloudticket.jwt")
public class JwtProperties {

    private ZoneJwt user = new ZoneJwt();
    private ZoneJwt merchant = new ZoneJwt();
    private ZoneJwt admin = new ZoneJwt();

    @Data
    public static class ZoneJwt {
        private String secret;      // 密钥，≥32 字符
        private Long ttlHours;      // 有效期（小时）
    }
}