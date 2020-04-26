package com.beeplay.easylog.log4j.appender;

import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.log4j.util.LogMessageUtil;
import com.beeplay.easylog.core.redis.RedisClient;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
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

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setReidsHost(String reidsHost) {
        this.reidsHost = reidsHost;
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
