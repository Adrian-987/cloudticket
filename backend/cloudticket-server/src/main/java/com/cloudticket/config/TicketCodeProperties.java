package com.cloudticket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 票码签名配置：cloudticket.ticket.secret
 * 密钥仅服务端持有（local.yml），绝不入库/不进 git；≥32 字符。
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudticket.ticket")
public class TicketCodeProperties {

    /** HMAC-SHA256 密钥 */
    private String secret;
}
