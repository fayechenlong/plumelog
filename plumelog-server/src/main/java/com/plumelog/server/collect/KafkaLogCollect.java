package com.plumelog.server.collect;

import com.plumelog.server.InitConfig;
import com.plumelog.server.es.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.kafka.KafkaConsumerClient;
import com.plumelog.server.util.DateUtil;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.*;
/**
 * className：KafkaLogCollect
 * description：KafkaLogCollect 获取kafka中日志，存储到es
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class KafkaLogCollect extends BaseLogCollect{

    private  org.slf4j.Logger logger= LoggerFactory.getLogger(KafkaLogCollect.class);
    private KafkaConsumer<String, String> kafkaConsumer;

    public KafkaLogCollect(String kafkaHosts,String esHosts,String userName,String passWord){
        super.elasticLowerClient= ElasticLowerClient.getInstance(esHosts,userName,passWord);
        this.kafkaConsumer=KafkaConsumerClient.getInstance(kafkaHosts, InitConfig.KAFKA_GROUP_NAME,InitConfig.MAX_SEND_SIZE).getKafkaConsumer();
        this.kafkaConsumer.subscribe(Arrays.asList(LogMessageConstant.LOG_KEY,LogMessageConstant.LOG_KEY+"_"+ LogMessageConstant.LOG_TYPE_TRACE));
        logger.info("sending log ready!");
    }
    public  void kafkaStart(){
        logger.info("getting log ready!");
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
            List<String> logList=new ArrayList();
            List<String> sendlogList=new ArrayList();
            records.forEach(record->{
                logger.debug("get log:" + record.value()+"  logType:"+record.topic());
                if(record.topic().equals(LogMessageConstant.LOG_KEY)){
                    logList.add(record.value());
                }
                if(record.topic().equals(LogMessageConstant.LOG_KEY_TRACE)){
                    sendlogList.add(record.value());
                }
            });
            if(logList.size()>0) {
                super.sendLog(LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD),logList);
            }
            if(sendlogList.size()>0){
                super.sendTraceLogList(LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD),sendlogList);
            }
        }
    }
}
