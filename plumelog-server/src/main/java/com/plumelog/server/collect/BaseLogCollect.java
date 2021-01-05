package com.plumelog.server.collect;

import com.plumelog.server.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.server.monitor.PlumelogMonitorEvent;
import com.plumelog.server.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
    public ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    public ElasticLowerClient elasticLowerClient;
    protected ApplicationEventPublisher applicationEventPublisher;

    public String getRunLogIndex(){
        //todo 根据命名空间设置索引文件
        if(InitConfig.ES_INDEX_MODEL.equals("day")) {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
        }else {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_TIME_FORMAT_YYYYMMDDHH);
        }
    }
    public String getTraceLogIndex(){
        //todo 根据命名空间设置索引文件
        if(InitConfig.ES_INDEX_MODEL.equals("day")) {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
        }else {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_TIME_FORMAT_YYYYMMDDHH);
        }
    }
    public String getQPSIndex(){
        if(InitConfig.ES_INDEX_MODEL.equals("day")) {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_QPS + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
        }else {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_QPS + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
        }
    }
    public void sendLog(String index, List<String> sendList) {
        try {
            if(sendList.size()>0) {
                long startTime=System.currentTimeMillis();
                elasticLowerClient.insertListLog(sendList, index, LogMessageConstant.ES_TYPE);
                long endTime=System.currentTimeMillis();
                logger.info("logList insert es success! count:{} RuningLog日志写入耗时：{}", sendList.size(),endTime-startTime);
            }
        } catch (Exception e) {
            //todo es写失败
            logger.error("logList insert es failed!", e);
        }
    }

    public void sendTraceLogList(String index, List<String> sendTraceLogList) {
        try {
            if(sendTraceLogList.size()>0) {
                elasticLowerClient.insertListTrace(sendTraceLogList, index, LogMessageConstant.ES_TYPE);
                logger.info("traceLogList insert es success! count:{}", sendTraceLogList.size());
            }
        } catch (Exception e) {
            logger.error("traceLogList insert es failed!", e);
        }
    }

    public void sendQPSLogList(String index, List<String> sendQPSLogList) {
        try {
            if(sendQPSLogList.size()>0) {
                elasticLowerClient.insertListTrace(sendQPSLogList, index, LogMessageConstant.ES_TYPE);
                logger.info("QPSLogList insert es success! count:{}", sendQPSLogList.size());
            }
        } catch (Exception e) {
            logger.error("QPSLogList insert es failed!", e);
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
