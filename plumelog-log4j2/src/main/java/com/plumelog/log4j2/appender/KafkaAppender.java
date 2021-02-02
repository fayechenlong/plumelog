package com.plumelog.log4j2.appender;

import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.kafka.KafkaProducerClient;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j2.util.LogMessageUtil;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：KafkaAppender
 * description：KafkaAppender 如果使用kafka作为队列用这个KafkaAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@Plugin(name = "KafkaAppender", category = "Core", elementType = "appender", printObject = true)
public class KafkaAppender extends AbstractAppender {
    private static KafkaProducerClient kafkaClient;
    private String appName;
    private String kafkaHosts;
    private String runModel;
    private String expand;
    private int maxCount=500;
    private int logQueueSize=10000;
    private int threadPoolSize=1;
    private String compressionType = "none";

    protected KafkaAppender(String name, String appName, String kafkaHosts, String runModel, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions, String expand,int maxCount,int logQueueSize,int threadPoolSize, String compressionType) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.kafkaHosts = kafkaHosts;
        this.runModel = runModel;
        this.expand = expand;
        this.maxCount=maxCount;
        this.logQueueSize=logQueueSize;
        this.threadPoolSize=threadPoolSize;
        this.compressionType = compressionType;
    }

    @Override
    public void append(LogEvent logEvent) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, logEvent);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, logEvent);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
        }

    }
    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();
    @PluginFactory
    public static KafkaAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("kafkaHosts") String kafkaHosts,
            @PluginAttribute("topic") String topic,
            @PluginAttribute("expand") String expand,
            @PluginAttribute("runModel") String runModel,
            @PluginAttribute("maxCount") int maxCount,
            @PluginAttribute("logQueueSize") int logQueueSize,
            @PluginAttribute("threadPoolSize") int threadPoolSize,
            @PluginAttribute("compressionType") String compressionType,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(runModel);
        }
        if (kafkaClient == null) {
            kafkaClient = KafkaProducerClient.getInstance(kafkaHosts, compressionType);
        }
        if (expand != null && LogMessageConstant.EXPANDS.contains(expand)) {
            LogMessageConstant.EXPAND = expand;
        }
        if(maxCount==0){
            maxCount=100;
        }
        if(logQueueSize==0){
            logQueueSize=10000;
        }
        if(threadPoolSize==0){
            threadPoolSize=1;
        }
        final int count=maxCount;
        MessageAppenderFactory.initQueue(logQueueSize);
        for(int a=0;a<threadPoolSize;a++){

            threadPoolExecutor.execute(()->{
                MessageAppenderFactory.startRunLog(kafkaClient,count);
            });
            threadPoolExecutor.execute(()->{
                MessageAppenderFactory.startTraceLog(kafkaClient,count);
            });
        }
        return new KafkaAppender(name, appName, kafkaHosts, runModel, filter, layout, true, expand,maxCount,logQueueSize,threadPoolSize, compressionType);
    }
}
