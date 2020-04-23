package com.beeplay.easylog.kafka;

import com.beeplay.easylog.redis.RedisClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

public class KafkaProducerClient {
    private static KafkaProducerClient instance;
    private KafkaProducerPool kafkaProducerPool;
    private Properties props = new Properties();
    public static KafkaProducerClient getInstance(String hosts) {
        if (instance == null) {
            synchronized (RedisClient.class) {
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
    public void pushMessage(String topic, String message) {
        KafkaProducer kafkaProducer=kafkaProducerPool.getResource();
        kafkaProducer.send(new ProducerRecord<String, String>(topic, message));
        kafkaProducerPool.returnResource(kafkaProducer);
    }
}
