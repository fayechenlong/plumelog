package com.beeplay.easylog.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.beeplay.easylog.core.MessageAppenderFactory;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.redis.RedisClient;
import com.beeplay.easylog.logback.util.LogMessageUtil;

import java.util.concurrent.atomic.AtomicInteger;


public class RedisAppender extends AppenderBase<ILoggingEvent> {
    private RedisClient redisClient;
    private String appName;
    private String reidsHost;
    private String redisPort;
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setReidsHost(String reidsHost) {
        this.reidsHost = reidsHost;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }


    @Override
    protected void append(ILoggingEvent event) {
        BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, event);
        MessageAppenderFactory.push(logMessage,this.redisClient);
    }

    @Override
    public void start() {
        super.start();
        redisClient = RedisClient.getInstance(this.reidsHost, Integer.parseInt(this.redisPort), "");
    }
}
