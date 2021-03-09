package com.plumelog.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
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
import com.plumelog.logback.util.LogMessageUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：RedisAppender
 * description：
 * time：2020-05-19.15:26
 *
 * @author Tank
 * @version 1.0.0
 */
public class RedisAppender extends AppenderBase<ILoggingEvent> {
    private AbstractClient redisClient;
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private String model = "standalone";
    private String masterName;
    private int redisDb = 0;
    private String runModel;
    private String expand;
    private int maxCount = 100;
    private int logQueueSize = 10000;
    private int threadPoolSize = 1;
    private boolean compressor = false;

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

    public void setRedisDb(int redisDb) {
        this.redisDb = redisDb;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setLogQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
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

    @Override
    protected void append(ILoggingEvent event) {
        send(event);
    }

    protected void send(ILoggingEvent event) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, event);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, event);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
        }
    }

    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();

    @Override
    public void start() {
        super.start();
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (this.expand != null && LogMessageConstant.EXPANDS.contains(this.expand)) {
            LogMessageConstant.EXPAND = this.expand;
        }
        if (this.redisClient == null) {

            if (this.model.equals("cluster")) {
                this.redisClient = RedisClusterClient.getInstance(this.redisHost, this.redisAuth);
            } else if (this.model.equals("sentinel")) {
                this.redisClient = RedisSentinelClient.getInstance(this.redisHost, this.masterName, this.redisAuth, this.redisDb);
            } else {
                int port = 6379;
                String ip = "127.0.0.1";
                if (this.redisPort == null) {
                    String[] hs = redisHost.split(":");
                    if (hs.length == 2) {
                        ip = hs[0];
                        port = Integer.valueOf(hs[1]);
                    }
                } else {
                    ip = this.redisHost;
                    port = Integer.parseInt(this.redisPort);
                }
                this.redisClient = RedisClient.getInstance(ip, port, this.redisAuth, this.redisDb);
            }
        }
        MessageAppenderFactory.initQueue(this.logQueueSize);
        for (int a = 0; a < this.threadPoolSize; a++) {
            threadPoolExecutor.execute(() -> MessageAppenderFactory.startRunLog(this.redisClient, this.maxCount,
                    this.compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY, this.compressor));
        }
        for (int a = 0; a < this.threadPoolSize; a++) {
            threadPoolExecutor.execute(() -> MessageAppenderFactory.startTraceLog(this.redisClient, this.maxCount,
                    this.compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE, this.compressor));
        }
    }
}
