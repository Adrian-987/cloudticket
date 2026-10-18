package com.cloudticket.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 * 乐观锁插件：票档库存/订单状态等带 version 字段的表，
 * UPDATE 时自动拼 version=version+1 WHERE version=旧值。
 * 分页插件待接入分页查询时再加（3.5.9+ 需配套引入 jsqlparser 依赖）。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //optimisticLocker为乐观锁插件,用MP写update自动拼接检查version
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 分页插件：必须指定 DbType，否则每次请求都要查库类型
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}
