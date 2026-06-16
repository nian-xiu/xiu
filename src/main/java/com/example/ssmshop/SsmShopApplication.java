package com.example.ssmshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.ssmshop.mapper")
public class SsmShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsmShopApplication.class, args);
    }
}
