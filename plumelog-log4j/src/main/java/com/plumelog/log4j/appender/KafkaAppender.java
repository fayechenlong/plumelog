package com.plumelog.log4j.appender;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.kafka.KafkaProducerClient;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：KafkaAppender
 * description：KafkaAppender 如果使用kafka作为队列用这个KafkaAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class KafkaAppender extends AppenderSkeleton {
    private KafkaProducerClient kafkaClient;
    private String appName;
    private String kafkaHosts;
    private String runModel;
    private String topic;
    private int maxCount=100;
    private int logQueueSize=10000;
    private int threadPoolSize=1;
    private String compressionType = "none";

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setKafkaHosts(String kafkaHosts) {
        this.kafkaHosts = kafkaHosts;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public void setKafkaClient(KafkaProducerClient kafkaClient) {
        this.kafkaClient = kafkaClient;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();
    @Override
    protected void append(LoggingEvent loggingEvent) {
        if(this.runModel!=null){
            LogMessageConstant.RUN_MODEL=Integer.parseInt(this.runModel);
        }
        if (this.kafkaClient == null) {
            this.kafkaClient = KafkaProducerClient.getInstance(this.kafkaHosts, this.compressionType);
            MessageAppenderFactory.rundataQueue=new LinkedBlockingQueue<>(this.logQueueSize);
            MessageAppenderFactory.tracedataQueue=new LinkedBlockingQueue<>(this.logQueueSize);
            for(int a=0;a<this.threadPoolSize;a++){

                threadPoolExecutor.execute(()->{
                    MessageAppenderFactory.startRunLog(this.kafkaClient,maxCount);
                });
                threadPoolExecutor.execute(()->{
                    MessageAppenderFactory.startTraceLog(this.kafkaClient,maxCount);
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
