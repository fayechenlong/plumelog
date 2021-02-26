package com.plumelog.log4j.appender;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.redis.RedisClusterClient;
import com.plumelog.core.redis.RedisSentinelClient;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.redis.RedisClient;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：RedisAppender
 * description：RedisAppender 如果使用redis作为队列用这个RedisAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisAppender extends AppenderSkeleton {
    private AbstractClient redisClient;
    private String appName;
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

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setLogQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
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

    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (this.redisClient == null) {
            if (this.model.equals("cluster")) {
                this.redisClient = RedisClusterClient.getInstance(this.redisHost, this.redisAuth);
            } else if (this.model.equals("sentinel")) {
                this.redisClient = RedisSentinelClient.getInstance(this.redisHost, this.masterName, this.redisAuth, this.redisDb);
            } else {
                int port = 6379;
                String ip = "127.0.0.1";
                if(this.redisPort==null){
                    String[] hs = redisHost.split(":");
                    if (hs.length == 2) {
                        ip = hs[0];
                        port = Integer.valueOf(hs[1]);
                    }
                }else {
                    ip=this.redisHost;
                    port=Integer.parseInt(this.redisPort);
                }
                this.redisClient = RedisClient.getInstance(ip,port,this.redisAuth, this.redisDb);
            }

            MessageAppenderFactory.initQueue(this.logQueueSize);
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
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, loggingEvent);
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
