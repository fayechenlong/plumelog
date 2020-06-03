package com.plumelog.log4j.appender;

import com.plumelog.log4j.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.redis.RedisClient;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;


public class RedisAppender extends AppenderSkeleton {
    private RedisClient redisClient;
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private String redisKey;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public void setRedisAuth(String redisAuth) {
        this.redisAuth = redisAuth;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (redisClient == null) {
            redisClient = RedisClient.getInstance(this.redisHost, Integer.parseInt(this.redisPort), this.redisAuth);
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, loggingEvent);
        MessageAppenderFactory.push(logMessage, redisClient);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
