package com.plumelog.core.kafka;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.exception.LogQueueConnectException;
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
    public static KafkaProducerClient getInstance(String hosts, String compressionType) {
        if (instance == null) {
            synchronized (KafkaProducerClient.class) {
                if (instance == null) {
                    instance = new KafkaProducerClient(hosts, compressionType);
                }
            }
        }
        return instance;
    }
    private KafkaProducerClient(String hosts, String compressionType){
        this.kafkaProducerPool = new KafkaProducerPool(hosts, compressionType);
    }
    @Override
    public void pushMessage(String topic, String message) throws LogQueueConnectException {
        KafkaProducer kafkaProducer=null;
        try {
            kafkaProducer=kafkaProducerPool.getResource();
            kafkaProducer.send(new ProducerRecord<String, String>(topic, message));
        }catch (Exception e){
            throw new LogQueueConnectException("kafka 写入失败！",e);
        }finally {
            if(kafkaProducer!=null){
                kafkaProducerPool.returnResource(kafkaProducer);
            }
        }

    }
    @Override
    public void putMessageList(String topic, List<String> list) throws LogQueueConnectException {
        KafkaProducer kafkaProducer=null;
        try {
            kafkaProducer=kafkaProducerPool.getResource();
            for(int a=0;a<list.size();a++){
                String message=list.get(a);
                kafkaProducer.send(new ProducerRecord<String, String>(topic, message));
            };
        }catch (Exception e){
            throw new LogQueueConnectException("kafka 写入失败！",e);
        }finally {
            if(kafkaProducer!=null){
                kafkaProducerPool.returnResource(kafkaProducer);
            }
        }

    }

}
