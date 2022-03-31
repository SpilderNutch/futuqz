package com.txx.springboot.futuqz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@MapperScan("com.txx.springboot.futuqz.mapper")
public class FutuqzApplication {

    public static void main(String[] args) {
        SpringApplication.run (FutuqzApplication.class, args);
    }

}
