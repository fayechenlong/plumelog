package com.beeplay.easylog.server.collect;

import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.kafka.KafkaConsumerClient;
import com.beeplay.easylog.server.InitConfig;
import com.beeplay.easylog.server.es.ElasticSearchClient;
import com.beeplay.easylog.server.util.DateUtil;
import com.beeplay.easylog.server.util.GfJsonUtil;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
* @Author Frank.chen
* @Description kafka collect
* @Date 14:15 2020/5/12
* @Param 
* @return 
**/
public class KafkaLogCollect extends BaseLogCollect{

    private  org.slf4j.Logger logger= LoggerFactory.getLogger(KafkaLogCollect.class);
    private KafkaConsumer<String, String> kafkaConsumer;

    public KafkaLogCollect(String kafkaHosts,String esHosts){
        super.elasticSearchClient=ElasticSearchClient.getInstance(esHosts);
        this.kafkaConsumer=KafkaConsumerClient.getInstance(kafkaHosts,InitConfig.KAFKA_GROUP_NAME,InitConfig.MAX_SEND_SIZE).getKafkaConsumer();
        this.kafkaConsumer.subscribe(Arrays.asList(LogMessageConstant.LOG_KEY,LogMessageConstant.LOG_KEY+"_"+ LogMessageConstant.LOG_TYPE_TRACE));
        logger.info("sending log ready!");
    }
    public  void kafkaStart(){
        logger.info("getting log ready!");
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
            records.forEach(record->{
                logger.debug("get log:" + record.value()+"  logType:"+record.topic());
                if(record.topic().equals(LogMessageConstant.LOG_KEY)){
                    super.logList.add(GfJsonUtil.parseObject(record.value(), Map.class));
                }
                if(record.topic().equals(LogMessageConstant.LOG_KEY+"_"+ LogMessageConstant.LOG_TYPE_TRACE)){
                    super.traceLogList.add(GfJsonUtil.parseObject(record.value(), Map.class));
                }
            });

            if(super.logList.size()>0) {
                final List<Map<String,Object>> logList=new CopyOnWriteArrayList();
                logList.addAll(super.logList);
                super.logList.clear();
                super.threadPoolExecutor.execute(() -> {
                    super.sendLog(LogMessageConstant.ES_INDEX + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD),logList);
                    super.logList.clear();
                });
            }
            if(super.traceLogList.size()>0) {
                final List<Map<String,Object>> sendlogList=new CopyOnWriteArrayList();
                sendlogList.addAll(super.traceLogList);
                super.traceLogList.clear();
                super.threadPoolExecutor.execute(() -> {
                    super.sendTraceLogList(LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD),sendlogList);
                    super.traceLogList.clear();
                });
            }

        }
    }
}
