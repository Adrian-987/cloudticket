package com.cloudticket.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomConfig {

    @Bean
    public RBloomFilter<Long> showBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> filter = redissonClient.getBloomFilter("cloudticket:show:bloom");
        filter.tryInit(100000, 0.01);   // 容量 10 万、误判率 1%（幂等，重复调没事）
        return filter;
    }

}
