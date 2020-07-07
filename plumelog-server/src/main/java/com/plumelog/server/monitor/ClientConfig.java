package com.plumelog.server.monitor;

import com.plumelog.core.redis.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * className：RedisClientConfig
 * description： TODO
 * time：2020-07-02.15:51
 *
 * @author Tank
 * @version 1.0.0
 */
@Configuration
public class RedisClientConfig {
    @Value("${plumelog.server.redis.redisHost:127.0.0.1:6379}")
    private String redisHost;
    @Value("${plumelog.server.redis.redisPassWord:}")
    private String redisPassWord;

    @Bean
    public RedisClient initRedisClient() {
        String[] hs = redisHost.split(":");
        int port = 6379;
        String ip = "127.0.0.1";
        if (hs.length == 2) {
            ip = hs[0];
            port = Integer.valueOf(hs[1]);
        }
        return RedisClient.getInstance(ip, port, redisPassWord);
    }
}
