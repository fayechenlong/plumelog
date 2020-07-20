package com.plumelog.ui.conf;

import com.plumelog.core.redis.RedisClient;
import com.plumelog.ui.es.ElasticLowerClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * className：ClientConfig
 * description： 客户端初始化
 * time：2020-07-02.15:51
 *
 * @author Tank
 * @version 1.0.0
 */
@Configuration
public class ClientConfig {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ClientConfig.class);
    @Value("${es.esHosts}")
    private String esHosts;
    @Value("${es.userName:}")
    private String userName;
    @Value("${es.passWord:}")
    private String passWord;
    @Value("${plumelog.server.redis.redisHost:127.0.0.1:6379}")
    private String redisHost;
    @Value("${plumelog.server.redis.redisPassWord:}")
    private String redisPassWord;


    @Bean
    public RedisClient initRedisClient() {
        if (StringUtils.isEmpty(redisHost)) {
            logger.error("can not find redisHost config! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
            return null;
        }
        String[] hs = redisHost.split(":");
        int port = 6379;
        String ip = "127.0.0.1";
        if (hs.length == 2) {
            ip = hs[0];
            port = Integer.valueOf(hs[1]);
        } else {
            logger.error("redis config error! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
            return null;
        }
        return RedisClient.getInstance(ip, port, redisPassWord);
    }

    @Bean
    public ElasticLowerClient initElasticLowerClient() {
        if (StringUtils.isEmpty(esHosts)) {
            logger.error("can not find esHosts config ! please check the plumelog.properties(plumelog.server.es.esHosts) ");
            return null;
        }
        return ElasticLowerClient.getInstance(esHosts, userName, passWord);
    }
}
