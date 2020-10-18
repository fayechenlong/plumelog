package com.plumelog.log4j.appender;

import com.plumelog.core.kafka.KafkaProducerClient;

/**
 * className：KafkaAppender
 * description：KafkaAppender 如果使用kafka作为队列用这个KafkaAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class KafkaAppender extends PlumelogAppender {
    private String kafkaHosts;

    public void setKafkaHosts(String kafkaHosts) {
        this.kafkaHosts = kafkaHosts;
    }


    protected void initializeClient() {
        this.plumelogClient = KafkaProducerClient.getInstance(this.kafkaHosts);
    }
}
