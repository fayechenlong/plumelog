package com.beeplay.easylog.log4j.appender;

import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.kafka.KafkaProducerClient;
import com.beeplay.easylog.log4j.util.LogMessageUtil;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import java.util.concurrent.ThreadPoolExecutor;


public class KafkaAppender extends AppenderSkeleton {
    private ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private KafkaProducerClient kafkaClient;
    private String appName;
    private String kafkaHosts;
    private String topic;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setKafkaHosts(String kafkaHosts) {
        this.kafkaHosts = kafkaHosts;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if(kafkaClient==null){
            kafkaClient= KafkaProducerClient.getInstance(kafkaHosts);
        }
        final String topic=this.topic;
        final LogMessage logMessage = LogMessageUtil.getLogMessage(this.appName,loggingEvent);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                kafkaClient.pushMessage(topic,GfJsonUtil.toJSONString(logMessage));
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
