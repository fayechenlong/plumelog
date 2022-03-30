package com.beeplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.embedded.RedisServer;

/**
 * className：LogServerStart
 * description：
 * time：2020/6/10  17:40
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class LogServerStartTest {

    public static void main( String[] args ){
        RedisServer redisServer;
        redisServer = RedisServer.builder()
                .port(6379) //端口
                .setting("bind 127.0.0.1") //绑定ip
                .build();
        redisServer.start();

    }
}
