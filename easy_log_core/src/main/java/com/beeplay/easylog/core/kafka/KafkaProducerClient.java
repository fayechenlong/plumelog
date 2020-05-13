package com.beeplay.easylog.core.kafka;

import com.beeplay.easylog.core.AbstractClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

public class KafkaProducerClient extends AbstractClient {
    private static KafkaProducerClient instance;
    private KafkaProducerPool kafkaProducerPool;
    public static KafkaProducerClient getInstance(String hosts) {
        if (instance == null) {
            synchronized (KafkaProducerClient.class) {
                if (instance == null) {
                    instance = new KafkaProducerClient(hosts);
                }
            }
        }
        return instance;
    }
    private KafkaProducerClient(String hosts){
        this.kafkaProducerPool=new KafkaProducerPool(hosts);
    }
    @Override
    public void pushMessage(String topic, String message) {
        KafkaProducer kafkaProducer=kafkaProducerPool.getResource();
        kafkaProducer.send(new ProducerRecord<String, String>(topic, message));
        kafkaProducerPool.returnResource(kafkaProducer);
    }
}
