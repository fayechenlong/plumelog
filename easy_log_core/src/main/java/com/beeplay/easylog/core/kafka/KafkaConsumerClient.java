package com.beeplay.easylog.core.kafka;


import com.beeplay.easylog.core.redis.RedisClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.util.Properties;

public class KafkaConsumerClient {
    private static KafkaConsumerClient instance;
    private KafkaConsumer kafkaConsumer;
    public static KafkaConsumerClient getInstance(String hosts) {
        if (instance == null) {
            synchronized (RedisClient.class) {
                if (instance == null) {
                    instance = new KafkaConsumerClient(hosts);
                }
            }
        }
        return instance;
    }

    public KafkaConsumer getKafkaConsumer() {
        return kafkaConsumer;
    }
    KafkaConsumerClient(String hosts){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "logConsumer");
        kafkaConsumer = new KafkaConsumer<>(props);
    }

}
