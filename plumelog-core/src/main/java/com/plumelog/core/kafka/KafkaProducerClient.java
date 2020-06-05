package com.plumelog.core.kafka;

import com.plumelog.core.AbstractClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
/**
 * className：KafkaProducerClient
 * description：kafka Producer instance
 * time：2020-05-11.16:17
 *
 * @author Frank.chen
 * @version 1.0.0
 */
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
    @Override
    public void putMessageList(String topic, List<String> list) {
        KafkaProducer kafkaProducer=kafkaProducerPool.getResource();
        kafkaProducerPool.returnResource(kafkaProducer);
    }
}
