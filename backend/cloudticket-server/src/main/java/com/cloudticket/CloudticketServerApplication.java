package com.cloudticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.cloudticket.mapper")
@EnableScheduling
public class CloudticketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudticketServerApplication.class, args);
    }

}
