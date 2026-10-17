package com.cloudticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cloudticket.mapper")
public class CloudticketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudticketServerApplication.class, args);
    }

}
