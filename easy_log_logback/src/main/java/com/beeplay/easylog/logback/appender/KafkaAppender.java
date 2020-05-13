package com.beeplay.easylog.logback.appender;


import ch.qos.logback.classic.spi.ILoggingEvent;
import com.beeplay.easylog.core.kafka.KafkaProducerClient;
import com.beeplay.easylog.core.util.ThreadPoolUtil;

import java.util.concurrent.ThreadPoolExecutor;


public class KafkaAppender extends AbstractAppender {
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

        if (kafkaClient == null) {
            kafkaClient = KafkaProducerClient.getInstance(this.kafkaHosts);
        }
        super.push(this.appName, event, kafkaClient);
    }
}
