package com.beeplay.easylog.core.appender;

import com.beeplay.easylog.LogMessage;
import com.beeplay.easylog.util.GfJsonUtil;
import com.beeplay.easylog.util.LogMessageUtil;
import com.beeplay.easylog.redis.RedisClient;
import com.beeplay.easylog.util.ThreadPoolUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import java.util.concurrent.ThreadPoolExecutor;


public class RedisAppender extends AppenderSkeleton {
    private ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private RedisClient redisClient;
    private String appName;
    private String reidsHost;
    private String redisPort;
    private String redisAuth;
    private String redisKey;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getReidsHost() {
        return reidsHost;
    }

    public void setReidsHost(String reidsHost) {
        this.reidsHost = reidsHost;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisAuth() {
        return redisAuth;
    }

    public void setRedisAuth(String redisAuth) {
        this.redisAuth = redisAuth;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if(redisClient==null) {
            redisClient = RedisClient.getInstance(this.reidsHost, Integer.parseInt(this.redisPort), this.redisAuth);
        }
        final String redisKey=this.redisKey;
        final LogMessage logMessage = LogMessageUtil.getLogMessage(this.appName,loggingEvent);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                redisClient.pushMessage(redisKey, GfJsonUtil.toJSONString(logMessage));
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
