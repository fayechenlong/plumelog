package com.plumelog.server.collect;

import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.server.monitor.PlumelogMonitorEvent;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：BaseLogCollect
 * description：BaseLogCollect 基类
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class BaseLogCollect {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(BaseLogCollect.class);
    public List<String> logList = new CopyOnWriteArrayList();
    public List<String> traceLogList = new CopyOnWriteArrayList();
    public ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    public ElasticLowerClient elasticLowerClient;
    protected ApplicationEventPublisher applicationEventPublisher;

    public void sendLog(String index, List<String> sendList) {
        try {
            elasticLowerClient.insertList(sendList, index, LogMessageConstant.ES_TYPE);
            logger.info("logList insert es success! count:{}", sendList.size());
        } catch (Exception e) {
            logger.error("logList insert es failed!", e);
        }
    }

    public void sendTraceLogList(String index, List<String> sendTraceLogList) {
        try {
            elasticLowerClient.insertList(sendTraceLogList, index, LogMessageConstant.ES_TYPE);
            logger.info("traceLogList insert es success! count:{}", sendTraceLogList.size());
        } catch (Exception e) {
            logger.error("traceLogList insert es failed!", e);
        }
    }

    protected void publisherMonitorEvent(List<String> logs) {
        if (logs.size()>0){
            try {
                applicationEventPublisher.publishEvent(new PlumelogMonitorEvent(this, logs));
            }catch (Exception e){
                logger.error("publisherMonitorEvent error!", e);
            }
        }

    }
}
