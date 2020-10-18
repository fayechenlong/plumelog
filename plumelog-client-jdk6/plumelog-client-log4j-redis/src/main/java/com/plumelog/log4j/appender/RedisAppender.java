package com.plumelog.log4j.appender;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.redis.RedisClient;

/**
 * className：RedisAppender
 * description：RedisAppender 如果使用redis作为队列用这个RedisAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisAppender extends PlumelogAppender {
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private int redisDb = 0;

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public void setRedisAuth(String redisAuth) {
        this.redisAuth = redisAuth;
    }

    public void setRedisDb(int redisDb) {
        this.redisDb = redisDb;
    }

    @Override
    protected void initializeClient() {
        this.plumelogClient = RedisClient.getInstance(this.redisHost, this.redisPort == null ?
                LogMessageConstant.REDIS_DEFAULT_PORT
                : Integer.parseInt(this.redisPort), this.redisAuth, this.redisDb);
    }
}
