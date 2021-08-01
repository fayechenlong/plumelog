package com.plumelog.log4j.appender;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.core.redis.RedisClusterClient;
import com.plumelog.core.redis.RedisSentinelClient;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j.util.LogMessageUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * className：RedisAppender
 * description：RedisAppender 如果使用redis作为队列用这个RedisAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisAppender extends AppenderSkeleton {
    private static final AtomicBoolean INIT = new AtomicBoolean();
    private static final ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private AbstractClient redisClient;
    private String appName;
    private String env = "default";
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private int redisDb = 0;
    private String runModel;
    private int maxCount = 100;
    private int logQueueSize = 10000;
    private int threadPoolSize = 1;
    private boolean compressor = false;
    private String model = "standalone";
    private String masterName;
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getRedisHost() {
        return redisHost;
    }
    
    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }
    
    public String getRedisPort() {
        return redisPort;
    }
    
    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public void setRedisAuth(String redisAuth) {
        this.redisAuth = redisAuth;
    }
    
    public int getRedisDb() {
        return redisDb;
    }
    
    public void setRedisDb(int redisDb) {
        this.redisDb = redisDb;
    }
    
    public String getRunModel() {
        return runModel;
    }
    
    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }
    
    public int getMaxCount() {
        return maxCount;
    }
    
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
    
    public int getLogQueueSize() {
        return logQueueSize;
    }
    
    public void setLogQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
    }
    
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
    
    public boolean isCompressor() {
        return compressor;
    }
    
    public void setCompressor(boolean compressor) {
        this.compressor = compressor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }
    
    public String getEnv() {
        return env;
    }
    
    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (this.redisClient == null) {
            MessageAppenderFactory.initQueue(this.logQueueSize);
            if ("cluster".equals(this.model)) {
                this.redisClient = RedisClusterClient.getInstance(this.redisHost, this.redisAuth);
            } else if ("sentinel".equals(this.model)) {
                this.redisClient = RedisSentinelClient
                        .getInstance(this.redisHost, this.masterName, this.redisAuth, this.redisDb);
            } else {
                int port = 6379;
                String ip = "127.0.0.1";
                if (this.redisPort == null) {
                    // 如果redisHost不包含:号则端口号默认使用6379
                    if (redisHost.contains(":")) {
                        String[] hs = redisHost.split(":");
                        if (hs.length == 2) {
                            ip = hs[0];
                            port = Integer.parseInt(hs[1]);
                        }
                    } else {
                        ip = this.redisHost;
                    }
                } else {
                    ip = this.redisHost;
                    port = Integer.parseInt(this.redisPort);
                }
                this.redisClient = RedisClient.getInstance(ip, port, this.redisAuth, this.redisDb);
            }

            // 当多并发情况下若redisClient为空时会多次执行以下代码，因此增加是否已初始化的判断
            if (INIT.compareAndSet(false, true)) {
                for (int a = 0; a < this.threadPoolSize; a++) {

                    threadPoolExecutor.execute(() -> {
                        MessageAppenderFactory.startRunLog(this.redisClient, maxCount,
                                this.compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY, this.compressor);
                    });
                    threadPoolExecutor.execute(() -> {
                        MessageAppenderFactory.startTraceLog(this.redisClient, maxCount,
                                this.compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE, this.compressor);
                    });
                }
            }
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, this.env, loggingEvent);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, loggingEvent);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
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
