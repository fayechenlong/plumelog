package com.beeplay.easylog.server.collect;


import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.redis.RedisClient;
import com.beeplay.easylog.server.InitConfig;
import com.beeplay.easylog.server.es.ElasticSearchClient;
import com.beeplay.easylog.server.util.DateUtil;
import com.beeplay.easylog.server.util.GfJsonUtil;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
* @Author Frank.chen
* @Description redis collect
* @Date 14:15 2020/5/12
* @Param 
* @return 
**/
public class RedisLogCollect extends BaseLogCollect{
    private  org.slf4j.Logger logger= LoggerFactory.getLogger(RedisLogCollect.class);
    private RedisClient redisClient;

    public RedisLogCollect(String redisHost,int redisPort,String esHosts){

        this.redisClient=RedisClient.getInstance(redisHost,redisPort,"");
        logger.info("getting log ready!");
        super.elasticSearchClient=ElasticSearchClient.getInstance(esHosts);
        logger.info("sending log ready!");
    }
    public  void redisStart(){

        threadPoolExecutor.execute(()->{
            collectRuningLog();
        });
        threadPoolExecutor.execute(()->{
            collectTraceLog();
        });

    }
    private  void collectRuningLog(){
        while (true) {
            List<String> logs=redisClient.getMessage(LogMessageConstant.LOG_KEY,InitConfig.MAX_SEND_SIZE);
            collect(logs,LogMessageConstant.ES_INDEX+ DateUtil.parseDateToStr(new Date(),DateUtil.DATE_FORMAT_YYYYMMDD));
        }
    }
    private  void collectTraceLog(){
        while (true) {
            List<String> logs=redisClient.getMessage(LogMessageConstant.LOG_KEY_TRACE,InitConfig.MAX_SEND_SIZE);
            collectTrace(logs,LogMessageConstant.ES_INDEX+LogMessageConstant.LOG_TYPE_TRACE+"_"+ DateUtil.parseDateToStr(new Date(),DateUtil.DATE_FORMAT_YYYYMMDD));
        }
    }
    private  void collect(List<String> logs,String index){
        if(logs.size()>0) {
            logs.forEach(log->{
                logger.debug("get log:" + log);
                Map<String, Object> map = GfJsonUtil.parseObject(log, Map.class);
                super.logList.add(map);
            });
            if (super.logList.size() > 0) {
                List<Map<String,Object>> logList=new CopyOnWriteArrayList();
                logList.addAll(super.logList);
                super.logList.clear();
                super.sendLog(index,logList);
            }
        }
        try {
            Thread.sleep(InitConfig.MAX_INTERVAL);
        } catch (InterruptedException e) {
            logger.error("",e);
        }
    }
    private  void collectTrace(List<String> logs,String index){
        if(logs.size()>0) {
            logs.forEach(log->{
                logger.debug("get log:" + log);
                Map<String, Object> map = GfJsonUtil.parseObject(log, Map.class);
                super.traceLogList.add(map);
            });
            if (super.traceLogList.size() > 0) {
                List<Map<String,Object>> logList=new CopyOnWriteArrayList();
                logList.addAll(super.traceLogList);
                super.traceLogList.clear();
                super.sendTraceLogList(index,logList);
            }
        }
        try {
            Thread.sleep(InitConfig.MAX_INTERVAL);
        } catch (InterruptedException e) {
            logger.error("",e);
        }
    }
}
