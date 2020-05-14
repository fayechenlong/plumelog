package com.beeplay.easylog;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan("com.beeplay")
public class AppStart {
    public static void main( String[] args ){
        SpringApplication.run(AppStart.class, args);
    }
}
