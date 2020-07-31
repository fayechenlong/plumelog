package com.plumelog.log4j.appender;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.log4j.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.redis.RedisClient;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * className：RedisAppender
 * description：RedisAppender 如果使用redis作为队列用这个RedisAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisAppender extends AppenderSkeleton {
    private RedisClient redisClient;
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private String redisKey;
    private String runModel;

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

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (redisClient == null) {
            redisClient = RedisClient.getInstance(this.redisHost, this.redisPort == null ?
                    LogMessageConstant.REDIS_DEFAULT_PORT
                    : Integer.parseInt(this.redisPort), this.redisAuth);
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, loggingEvent);
        final String message=LogMessageUtil.getLogMessage(logMessage,loggingEvent);
        if(logMessage instanceof RunLogMessage){
            MessageAppenderFactory.push(LogMessageConstant.LOG_KEY,message, redisClient, "plume.log.ack");
        }else {
            MessageAppenderFactory.push(logMessage, redisClient, "plume.log.ack");
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
