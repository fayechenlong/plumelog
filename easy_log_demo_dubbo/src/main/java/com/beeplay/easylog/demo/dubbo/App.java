package com.beeplay.easylog.demo.dubbo;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
* @Author Frank.chen
* @Description //TODO
* @Date 15:58 2020/5/13
* @Param 
* @return 
**/
@SpringBootApplication
@EnableDubbo
public class App {
    public static void main( String[] args ){
        SpringApplication.run(App.class, args);
    }
}
