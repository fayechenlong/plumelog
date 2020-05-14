package com.beeplay.easylog;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * className：App
 * description： TODO
 * time：2020-05-14.11:03
 *
 * @author Tank
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDubbo
public class App {
    public static void main( String[] args ){
        SpringApplication.run(App.class, args);
    }
}
