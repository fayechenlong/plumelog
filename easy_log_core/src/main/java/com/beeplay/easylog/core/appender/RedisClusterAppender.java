package com.beeplay.easylog.core.appender;

import com.beeplay.easylog.LogMessage;
import com.beeplay.easylog.util.GfJsonUtil;
import com.beeplay.easylog.util.LogMessageUtil;
import com.beeplay.easylog.redis.RedisClusterClient;
import com.beeplay.easylog.util.ThreadPoolUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import java.util.concurrent.ThreadPoolExecutor;


public class RedisClusterAppender extends AppenderSkeleton {
    private ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private RedisClusterClient redisClusterClient;
    private String appName;
    private String reidsHosts;
    private String redisKey;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getReidsHosts() {
        return reidsHosts;
    }

    public void setReidsHosts(String reidsHosts) {
        this.reidsHosts = reidsHosts;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (redisClusterClient == null) {
            redisClusterClient = RedisClusterClient.getInstance(reidsHosts);
        }
        final String redisKey = this.redisKey;
        final LogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, loggingEvent);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                redisClusterClient.pushMessage(redisKey, GfJsonUtil.toJSONString(logMessage));
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
