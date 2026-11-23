package com.cloudticket.config;

import com.cloudticket.pojo.vo.UserShowDetailVO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LocalCacheConfig {
    @Bean
    public Cache<Long, UserShowDetailVO> showDetailLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)                          // 最多 1000 条，超了按 W-TinyLFU 淘汰最少用的
                .expireAfterWrite(Duration.ofMinutes(5))   // 写入 5 分钟后过期（兜底一致性）
                .recordStats()                             // 开启统计（可看命中率）
                .build();
    }
}
