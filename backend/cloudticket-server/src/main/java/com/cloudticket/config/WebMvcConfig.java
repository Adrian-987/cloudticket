package com.cloudticket.config;

import com.cloudticket.interceptor.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtProperties jwtProperties;

    public WebMvcConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new JwtAuthInterceptor(jwtProperties.getUser().getSecret(), "userId"))
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/auth/login", "/user/auth/register");

        registry.addInterceptor(new JwtAuthInterceptor(
                        jwtProperties.getMerchant().getSecret(), "merchantId"))
                .addPathPatterns("/merchant/**")
                .excludePathPatterns("/merchant/auth/login");

        registry.addInterceptor(new JwtAuthInterceptor(
                        jwtProperties.getAdmin().getSecret(), "adminId"))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login");

    }

    // 通过CORS,实现跨域资源共享,实现前后端分离
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
