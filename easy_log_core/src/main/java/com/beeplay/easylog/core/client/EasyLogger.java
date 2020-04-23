package com.beeplay.easylog.core.client;

import com.beeplay.easylog.util.LogExceptionStackTrace;
import com.beeplay.easylog.redis.RedisClient;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;

public class EasyLogger {
    private static EasyLogger easyLogger;
    private Logger logger;
    public static EasyLogger getLogger(Logger Logger) {
        if (easyLogger == null) {
            synchronized (RedisClient.class) {
                if (easyLogger == null) {
                    easyLogger = new EasyLogger(Logger);
                }
            }
        }
        return easyLogger;
    }
    EasyLogger(Logger logger){
      this.logger=logger;
    }
    public void info(String message){
        String transId=TransId.logTranID.get();
        if(transId!=null&&!"".equals(transId)) {
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("transId", transId);
            map.put("content", message);
            logger.info(map);
        }else {
            logger.info(message);
        }
    }
    public void info(String message,String transId){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("transId", transId);
        map.put("content", message);
        logger.info(map);
    }
    public void error(String message){
        String transId=TransId.logTranID.get();
        if(transId!=null&&!"".equals(transId)) {
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("transId", transId);
            map.put("content", message);
            logger.error(map);
        }else {
            logger.error(message);
        }
    }
    public void error(String message,String transId){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("transId", transId);
        map.put("content", message);
        logger.error(map);
    }
    public void error(String message,Exception e){
        String transId=TransId.logTranID.get();
        if(transId!=null&&!"".equals(transId)) {
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("transId", transId);
            map.put("content", message+";Exception="+ LogExceptionStackTrace.erroStackTrace(e).toString());
            logger.error(map);
        }else {
            logger.error(message+";Exception="+ LogExceptionStackTrace.erroStackTrace(e).toString());
        }
    }
    public void warn(String message){
        String transId=TransId.logTranID.get();
        if(transId!=null&&!"".equals(transId)) {
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("transId", transId);
            map.put("content", message);
            logger.warn(map);
        }else {
            logger.warn(message);
        }
    }
    public void warn(String message,String transId){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("transId", transId);
        map.put("content", message);
        logger.warn(map);
    }
    public void debug(String message){
        String transId=TransId.logTranID.get();
        if(transId!=null&&!"".equals(transId)) {
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("transId", transId);
            map.put("content", message);
            logger.debug(map);
        }else {
            logger.debug(message);
        }
    }
    public void debug(String message,String transId){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("transId", transId);
        map.put("content", message);
        logger.debug(map);
    }
}
