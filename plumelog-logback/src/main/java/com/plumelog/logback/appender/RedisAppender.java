package com.plumelog.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.logback.util.LogMessageUtil;

/**
 * className：RedisAppender
 * description：
 * time：2020-05-19.15:26
 *
 * @author Tank
 * @version 1.0.0
 */
public class RedisAppender extends AppenderBase<ILoggingEvent> {
    private RedisClient redisClient;
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private String runModel;
    private String expand;

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

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

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    @Override
    protected void append(ILoggingEvent event) {
        BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, event);
        MessageAppenderFactory.push(logMessage, redisClient, "plume.log.ack");
    }

    @Override
    public void start() {
        super.start();
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (expand != null && LogMessageConstant.EXPANDS.contains(expand)) {
            LogMessageConstant.EXPAND = expand;
        }
        if (redisClient == null) {
            redisClient = RedisClient.getInstance(this.redisHost,
                    this.redisPort == null ?
                            LogMessageConstant.REDIS_DEFAULT_PORT
                            : Integer.parseInt(this.redisPort),
                    this.redisAuth);
        }
    }
}
