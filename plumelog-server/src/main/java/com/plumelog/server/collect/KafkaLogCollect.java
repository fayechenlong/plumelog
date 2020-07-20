package com.plumelog.server.collect;

import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.server.util.DateUtil;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.*;

/**
 * className：KafkaLogCollect
 * description：KafkaLogCollect 获取kafka中日志，存储到es
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class KafkaLogCollect extends BaseLogCollect {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(KafkaLogCollect.class);

    private KafkaConsumer<String, String> kafkaConsumer;

    public KafkaLogCollect(ElasticLowerClient elasticLowerClient, KafkaConsumer kafkaConsumer, ApplicationEventPublisher applicationEventPublisher) {
        super.elasticLowerClient = elasticLowerClient;
        this.kafkaConsumer = kafkaConsumer;
        this.kafkaConsumer.subscribe(Arrays.asList(LogMessageConstant.LOG_KEY, LogMessageConstant.LOG_KEY_TRACE));
        super.applicationEventPublisher = applicationEventPublisher;
        logger.info("kafkaConsumer subscribe ready!");
        logger.info("sending log ready!");
    }

    public void kafkaStart() {
        threadPoolExecutor.execute(() -> {
            collectRuningLog();
        });
        logger.info("KafkaLogCollect is starting!");
    }

    public void collectRuningLog() {
        while (true) {
            List<String> logList = new ArrayList();
            List<String> sendlogList = new ArrayList();
            try {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                records.forEach(record -> {
                    logger.debug("get log:" + record.value() + "  logType:" + record.topic());
                    if (record.topic().equals(LogMessageConstant.LOG_KEY)) {
                        logList.add(record.value());
                    }
                    if (record.topic().equals(LogMessageConstant.LOG_KEY_TRACE)) {
                        sendlogList.add(record.value());
                    }
                });
            } catch (Exception e) {
                logger.error("get logs from kafka failed! ", e);
            }
            if (logList.size() > 0) {
                publisherMonitorEvent(logList);
                super.sendLog(LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD), logList);
            }
            if (sendlogList.size() > 0) {
                super.sendTraceLogList(LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD), sendlogList);
            }
        }
    }
}
