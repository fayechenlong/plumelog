package com.plumelog.server.monitor;

import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.client.ElasticLowerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * className：RedisClientConfig
 * description： TODO
 * time：2020-07-02.15:51
 *
 * @author Tank
 * @version 1.0.0
 */
@Configuration
public class ClientConfig {
    @Value("${plumelog.server.redis.redisHost:127.0.0.1:6379}")
    private String redisHost;
    @Value("${plumelog.server.redis.redisPassWord:}")
    private String redisPassWord;

    @Value("${plumelog.server.es.esHosts:}")
    private String esHosts;
    @Value("${plumelog.server.es.indexType:}")
    private String indexType;
    @Value("${plumelog.server.es.userName:}")
    private String esUserName;
    @Value("${plumelog.server.es.passWord:}")
    private String esPassWord;

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

    @Bean
    public ElasticLowerClient initElasticLowerClient() {
        if (StringUtils.isEmpty(esHosts)) {
            return null;
        }
        if (StringUtils.isEmpty(esUserName)) {
            return null;
        }
        return ElasticLowerClient.getInstance(esHosts, esUserName, esPassWord);
    }
}
