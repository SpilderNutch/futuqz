package com.txx.springboot.futuqz.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "futu")
public class FutuConfig {


    private String serverIp;

    private int serverPort;





}
