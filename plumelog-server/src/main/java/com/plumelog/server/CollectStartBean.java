package com.plumelog.server;

import com.plumelog.core.constant.LogMessageConstant;
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

import java.util.Calendar;
import java.util.Date;

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

    private String getRunLogIndex(Date date){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD);
    }
    private String getTraceLogIndex(Date date){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD);
    }
    private String getRunLogIndex(Date date,String hour){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD)+hour;
    }
    private String getTraceLogIndex(Date date,String hour){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD)+hour;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            serverStart();


            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1);
            Date date = cal.getTime();
            if(InitConfig.ES_INDEX_MODEL.equals("day")) {
                creatIndiceLog(getRunLogIndex(date));
                creatIndiceTrace(getRunLogIndex(date));
            }else {
                for (int a = 0; a < 24; a++) {
                    String hour=String.format("%02d",a);
                    creatIndiceLog(getRunLogIndex(date,hour));
                    creatIndiceTrace(getRunLogIndex(date,hour));

                }
            }

        } catch (Exception e) {
            logger.error("plumelog server starting failed!", e);
        }
    }


    private void creatIndiceLog(String index){
        if(!elasticLowerClient.existIndice(index)){
            elasticLowerClient.creatIndice(index);
        };
    }
    private void creatIndiceTrace(String index){
        if(!elasticLowerClient.existIndice(index)){
            elasticLowerClient.creatIndiceTrace(index);
        };
    }
}
