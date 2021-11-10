package com.plumelog.springboot.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class AppStart {
    public static void main( String[] args ){
        SpringApplication.run(AppStart.class, args);
    }
}
