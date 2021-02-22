package com.plumelog.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.redis.RedisClusterClient;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.logback.util.LogMessageUtil;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：RedisClusterAppender
 * @author zhanghongzheng
 * @version 1.0.0
 */
public class RedisClusterAppender extends AppenderBase<ILoggingEvent> {
    private RedisClusterClient redisClusterClient;
    private String appName;
    private String redisClusterNodes;
    private String redisAuth;
    private String runModel;
    private String expand;
    private int maxCount=100;
    private int logQueueSize=10000;
    private int threadPoolSize=1;

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setRedisClusterNodes(String redisClusterNodes) {
        this.redisClusterNodes = redisClusterNodes;
    }

    public void setRedisAuth(String redisAuth) {
        this.redisAuth = redisAuth;
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

    @Override
    protected void append(ILoggingEvent event) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, event);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, event);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
        }
    }
    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();
    @Override
    public void start() {

		if(this.redisClusterNodes==null||"".equals(this.redisClusterNodes)){
			return;
		}

        super.start();
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (this.expand != null && LogMessageConstant.EXPANDS.contains(this.expand)) {
            LogMessageConstant.EXPAND = this.expand;
        }

        if (this.redisClusterClient == null) {
            this.redisClusterClient = RedisClusterClient.getInstance(this.redisClusterNodes, this.redisAuth);
        }
        MessageAppenderFactory.initQueue(this.logQueueSize);
        for (int a = 0; a < this.threadPoolSize; a++) {
            threadPoolExecutor.execute(() -> {
                MessageAppenderFactory.startRunLog(this.redisClusterClient, this.maxCount);
            });
        }
        for (int a = 0; a < this.threadPoolSize; a++) {
            threadPoolExecutor.execute(() -> {
                MessageAppenderFactory.startTraceLog(this.redisClusterClient, this.maxCount);
            });
        }


    }
}
