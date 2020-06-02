package com.plumelog.server.collect;


import com.plumelog.server.InitConfig;
import com.plumelog.server.es.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.util.DateUtil;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public RedisLogCollect(String redisHost,int redisPort,String esHosts,String userName,String passWord){

        this.redisClient=RedisClient.getInstance(redisHost,redisPort,"");
        logger.info("getting log ready!");
        super.elasticLowerClient= ElasticLowerClient.getInstance(esHosts,userName,passWord);
        logger.info("sending log ready!");
    }
    public RedisLogCollect(String redisHost,int redisPort,String redisPassWord,String esHosts,String userName,String passWord){

        this.redisClient=RedisClient.getInstance(redisHost,redisPort,redisPassWord);
        logger.info("getting log ready!");
        super.elasticLowerClient= ElasticLowerClient.getInstance(esHosts,userName,passWord);
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
            List<String> logs=redisClient.getMessage(LogMessageConstant.LOG_KEY, InitConfig.MAX_SEND_SIZE);
            collect(logs,LogMessageConstant.ES_INDEX+ LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(),DateUtil.DATE_FORMAT_YYYYMMDD));
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
                super.logList.add(log);
            });
            if (super.logList.size() > 0) {
                List<String> logList=new ArrayList();
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
                super.traceLogList.add(log);
            });
            if (super.traceLogList.size() > 0) {
                List<String> logList=new ArrayList();
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
