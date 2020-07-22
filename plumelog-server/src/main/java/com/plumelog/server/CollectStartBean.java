package com.plumelog.server;

import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.server.collect.KafkaLogCollect;
import com.plumelog.server.collect.RedisLogCollect;
import com.plumelog.server.collect.RestLogCollect;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * className：CollectStartBean
 * description：日誌搜集spring bean
 * time：2020/6/10  17:44
 *
 * @author frank.chen
 * @version 1.0.0
 */
@Component
@Order(100)
public class CollectStartBean implements InitializingBean {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(CollectStartBean.class);

    @Autowired
    private ElasticLowerClient elasticLowerClient;
    @Autowired
    private RedisClient redisClient;
    @Autowired(required = false)
    private KafkaConsumer kafkaConsumer;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private void serverStart() {
        if (InitConfig.KAFKA_MODE_NAME.equals(InitConfig.START_MODEL)) {
            KafkaLogCollect kafkaLogCollect = new KafkaLogCollect(elasticLowerClient, kafkaConsumer, applicationEventPublisher);
            kafkaLogCollect.kafkaStart();
        }
        if (InitConfig.REDIS_MODE_NAME.equals(InitConfig.START_MODEL)) {
            RedisLogCollect redisLogCollect = new RedisLogCollect(elasticLowerClient, redisClient, applicationEventPublisher);
            redisLogCollect.redisStart();
        }
        if (InitConfig.REST_MODE_NAME.equals(InitConfig.START_MODEL)) {
            RestLogCollect restLogCollect = new RestLogCollect(elasticLowerClient, applicationEventPublisher);
            restLogCollect.restStart();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            serverStart();
        } catch (Exception e) {
            logger.error("plumelog server starting failed!", e);
        }
    }
}
