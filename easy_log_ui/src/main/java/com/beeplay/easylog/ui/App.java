package com.beeplay.easylog.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
* @Author Frank.chen
* @Description //TODO
* @Date 11:49 2020/5/18
* @Param
* @return
**/
@SpringBootApplication
@ComponentScan(basePackages = {"com.beeplay"})
public class App 
{
    public static void main( String[] args ){
        SpringApplication.run(App.class, args);
    }
}
