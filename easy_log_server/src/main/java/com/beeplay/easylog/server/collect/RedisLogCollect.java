package com.beeplay.easylog.server.collect;


import com.beeplay.easylog.core.redis.RedisClient;
import com.beeplay.easylog.server.InitConfig;
import com.beeplay.easylog.server.es.ElasticSearchClient;
import com.beeplay.easylog.server.util.DateUtil;
import com.beeplay.easylog.server.util.GfJsonUtil;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class RedisLogCollect {
    private static Logger logger=Logger.getLogger(RedisLogCollect.class);
    private static List<Map<String,Object>> list=new CopyOnWriteArrayList();

    public static void redisStart(String redisHot,int redisPort,String esHosts){

        RedisClient redisClient=RedisClient.getInstance(redisHot,redisPort,"");
        logger.info("getting log ready!");
        ElasticSearchClient ec=ElasticSearchClient.getInstance(esHosts);
        logger.info("sending log ready!");
        while (true) {
            List<String> logs=redisClient.getMessage(InitConfig.LOG_KEY,InitConfig.MAX_SEND_SIZE);
            if(logs.size()>0) {
                logger.info("get log " + " " + list.size() + " counts!");
                for (String log : logs) {
                    logger.info("get log:" + log);
                    Map<String, Object> map = GfJsonUtil.parseObject(log, Map.class);
                    list.add(map);
                }
                if (list.size() > 0) {
                    sendLog(ec);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private static void sendLog(ElasticSearchClient ec){
        try {
            ec.insertList(list,InitConfig.ES_INDEX+ DateUtil.parseDateToStr(new Date(),DateUtil.DATE_FORMAT_YYYYMMDD),InitConfig.ES_TYPE);
            logger.info("log insert es success! count:"+list.size());
            list.clear();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
