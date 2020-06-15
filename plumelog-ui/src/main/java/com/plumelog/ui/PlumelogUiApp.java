package com.plumelog.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.plumelog"})
public class PlumelogUiApp {
    public static void main(String[] args) {
        SpringApplication.run(PlumelogUiApp.class, args);
    }
}
