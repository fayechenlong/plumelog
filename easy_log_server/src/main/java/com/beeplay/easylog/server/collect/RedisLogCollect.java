package com.beeplay.easylog.server.collect;


import com.beeplay.easylog.core.LogTypeContext;
import com.beeplay.easylog.core.redis.RedisClient;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import com.beeplay.easylog.server.InitConfig;
import com.beeplay.easylog.server.es.ElasticSearchClient;
import com.beeplay.easylog.server.util.DateUtil;
import com.beeplay.easylog.server.util.GfJsonUtil;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class RedisLogCollect {
    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(RedisLogCollect.class);
    private static List<Map<String,Object>> list=new CopyOnWriteArrayList();

    public static void redisStart(String redisHot,int redisPort,String esHosts){

        RedisClient redisClient=RedisClient.getInstance(redisHot,redisPort,"");
        logger.info("getting log ready!");
        ElasticSearchClient ec=ElasticSearchClient.getInstance(esHosts);
        logger.info("sending log ready!");
        threadPoolExecutor.execute(()->{
            collectRuningLog(redisClient,ec);
        });
        threadPoolExecutor.execute(()->{
            collectTraceLog(redisClient,ec);
        });


    }
    public static void collectRuningLog(RedisClient redisClient, ElasticSearchClient ec){
        while (true) {
            List<String> logs=redisClient.getMessage(InitConfig.LOG_KEY,InitConfig.MAX_SEND_SIZE);
            collect(logs,ec,InitConfig.ES_INDEX+ DateUtil.parseDateToStr(new Date(),DateUtil.DATE_FORMAT_YYYYMMDD));
        }
    }
    public static void collectTraceLog(RedisClient redisClient, ElasticSearchClient ec){
        while (true) {
            List<String> logs=redisClient.getMessage(InitConfig.LOG_KEY+"_"+ LogTypeContext.LOG_TYPE_TRACE,InitConfig.MAX_SEND_SIZE);
            collect(logs,ec,InitConfig.ES_INDEX+LogTypeContext.LOG_TYPE_TRACE+"_"+ DateUtil.parseDateToStr(new Date(),DateUtil.DATE_FORMAT_YYYYMMDD));
        }
    }
    private static void collect(List<String> logs,ElasticSearchClient ec,String index){
        if(logs.size()>0) {
            logger.info("get log " + " " + list.size() + " counts!");
            logs.forEach(log->{
                logger.info("get log:" + log);
                Map<String, Object> map = GfJsonUtil.parseObject(log, Map.class);
                list.add(map);
            });
            if (list.size() > 0) {
                sendLog(ec,InitConfig.ES_INDEX+ DateUtil.parseDateToStr(new Date(),DateUtil.DATE_FORMAT_YYYYMMDD));
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static void sendLog(ElasticSearchClient ec,String index){
        try {
            ec.insertList(list,index,InitConfig.ES_TYPE);
            logger.info("log insert es success! count:"+list.size());
            list.clear();
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
