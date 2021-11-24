package com.plumelog.demo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDubbo
@ServletComponentScan
@EnableScheduling
public class AppStart {
    public static void main( String[] args ){
        SpringApplication.run(AppStart.class, args);
    }
}
