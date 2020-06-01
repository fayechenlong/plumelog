package com.plumelog.core.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.util.Properties;

public class KafkaConsumerClient {
    private static KafkaConsumerClient instance;
    private KafkaConsumer kafkaConsumer;
    public static KafkaConsumerClient getInstance(String hosts,String groupName,int maxPullSize) {
        if (instance == null) {
            synchronized (KafkaConsumerClient.class) {
                if (instance == null) {
                    instance = new KafkaConsumerClient(hosts,groupName,maxPullSize);
                }
            }
        }
        return instance;
    }

    public KafkaConsumer getKafkaConsumer() {
        return kafkaConsumer;
    }
    KafkaConsumerClient(String hosts,String groupName,int maxPullSize){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPullSize);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        kafkaConsumer = new KafkaConsumer<>(props);
    }

}
