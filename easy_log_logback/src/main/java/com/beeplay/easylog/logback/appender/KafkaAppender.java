package com.beeplay.easylog.logback.appender;



import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.kafka.KafkaProducerClient;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import com.beeplay.easylog.logback.util.LogMessageUtil;

import java.util.concurrent.ThreadPoolExecutor;


public class KafkaAppender  extends AppenderBase<ILoggingEvent> {
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
    protected void append(ILoggingEvent event) {

        if(kafkaClient==null){
            kafkaClient= KafkaProducerClient.getInstance(this.kafkaHosts);
        }
        final String topic=this.topic;
        final LogMessage logMessage = LogMessageUtil.getLogMessage(this.appName,event);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                kafkaClient.pushMessage(topic, GfJsonUtil.toJSONString(logMessage));
            }
        });
    }
}
