package com.plumelog.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * className：LogServerStart
 * description：
 * time：2020/6/10  17:40
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.plumelog"})
public class LogServerStart {

    public static void main(String[] args) {
        SpringApplication.run(LogServerStart.class, args);
    }
}
