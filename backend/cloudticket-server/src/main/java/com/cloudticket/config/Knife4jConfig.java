package com.cloudticket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档：启动后访问 /doc.html 在线调试
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI cloudticketOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("云票 CloudTicket API")
                .description("票务预订平台接口文档（P1 骨架阶段）")
                .version("v1.0"));
    }
}
