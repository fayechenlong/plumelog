package com.beeplay.easylog.server.collect;

import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import com.beeplay.easylog.server.es.ElasticLowerClient;
import com.beeplay.easylog.server.es.ElasticSearchClient;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName BaseLogCollect
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/12 15:13
 * @Version 1.0
 **/
public class BaseLogCollect {
    private  org.slf4j.Logger logger= LoggerFactory.getLogger(BaseLogCollect.class);
    public List<String> logList=new CopyOnWriteArrayList();
    public List<String> traceLogList=new CopyOnWriteArrayList();

    public ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();

    public ElasticSearchClient elasticSearchClient;
    public ElasticLowerClient elasticLowerClient;

    public  void sendLog(String index,List<String> sendList){
        try {
            elasticLowerClient.insertList(sendList,index, LogMessageConstant.ES_TYPE);
            logger.info("logList insert es success! count:"+sendList.size());
        } catch (Exception e) {
            logger.error("",e);
        }
    }
    public  void sendTraceLogList(String index,List<String> sendTraceLogList){
        try {
            elasticLowerClient.insertList(sendTraceLogList,index,LogMessageConstant.ES_TYPE);
            logger.info("traceLogList insert es success! count:"+sendTraceLogList.size());
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
