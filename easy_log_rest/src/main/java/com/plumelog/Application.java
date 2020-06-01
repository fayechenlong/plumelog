package com.plumelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * className：Application
 * description： TODO
 * time：2020-05-29.15:14
 *
 * @author Tank
 * @version 1.0.0
 */
@EnableFeignClients(basePackages = {"com.plumelog"})
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
