package com.plumelog.logback.appender;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.kafka.KafkaProducerClient;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.logback.util.LogMessageUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：KafkaAppender
 * description：KafkaAppender 如果使用kafka作为队列用这个KafkaAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class KafkaAppender extends AppenderBase<ILoggingEvent> {
    private KafkaProducerClient kafkaClient;
    private String appName;
    private String kafkaHosts;
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

    public void setKafkaHosts(String kafkaHosts) {
        this.kafkaHosts = kafkaHosts;
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
        super.start();
        if(this.runModel!=null){
            LogMessageConstant.RUN_MODEL=Integer.parseInt(this.runModel);
        }
        if (this.kafkaClient == null) {
            this.kafkaClient = KafkaProducerClient.getInstance(this.kafkaHosts);
        }
        if (this.expand != null && LogMessageConstant.EXPANDS.contains(expand)) {
            LogMessageConstant.EXPAND = expand;
        }

        if(MessageAppenderFactory.rundataQueue==null) {
            MessageAppenderFactory.rundataQueue = new LinkedBlockingQueue<>(this.logQueueSize);
            for (int a = 0; a < this.threadPoolSize; a++) {
                threadPoolExecutor.execute(() -> {
                    MessageAppenderFactory.startRunLog(this.kafkaClient, this.maxCount);
                });
            }
        }
        if(MessageAppenderFactory.tracedataQueue==null) {
            MessageAppenderFactory.tracedataQueue = new LinkedBlockingQueue<>(this.logQueueSize);
            for (int a = 0; a < this.threadPoolSize; a++) {
                threadPoolExecutor.execute(() -> {
                    MessageAppenderFactory.startTraceLog(this.kafkaClient, this.maxCount);
                });
            }
        }
    }
}
