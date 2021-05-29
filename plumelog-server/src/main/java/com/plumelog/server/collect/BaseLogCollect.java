package com.plumelog.server.collect;

import com.alibaba.fastjson.JSON;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.server.InitConfig;
import com.plumelog.server.cache.AppNameCache;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.server.monitor.PlumelogMonitorEvent;
import com.plumelog.server.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
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
    protected ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    public ElasticLowerClient elasticLowerClient;
    protected ApplicationEventPublisher applicationEventPublisher;

    public String getRunLogIndex(){
        if(InitConfig.ES_INDEX_MODEL.equals("day")) {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
        }else {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_TIME_FORMAT_YYYYMMDDHH);
        }
    }
    public String getTraceLogIndex(){
        if(InitConfig.ES_INDEX_MODEL.equals("day")) {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD);
        }else {
            return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_TIME_FORMAT_YYYYMMDDHH);
        }
    }

    public void sendLog(String index, List<String> sendList) {
        try {
            if(sendList.size()>0) {
                elasticLowerClient.insertListLog(sendList, index, LogMessageConstant.ES_TYPE);
            }
        } catch (Exception e) {
            logger.error("logList insert es failed!", e);
        }
    }

    public void sendTraceLogList(String index, List<String> sendTraceLogList) {
        try {
            if(sendTraceLogList.size()>0) {
                elasticLowerClient.insertListTrace(sendTraceLogList, index, LogMessageConstant.ES_TYPE);
            }
        } catch (Exception e) {
            logger.error("traceLogList insert es failed!", e);
        }
    }

    protected void publisherMonitorEvent(List<String> logs) {
        int size=logs.size();
        if (size>0){
            try {
                List<RunLogMessage> errorLogs = new ArrayList<>();
                for(int i=0;i<size;i++) {
                    String logString = logs.get(i);
                    RunLogMessage runLogMessage = JSON.parseObject(logString, RunLogMessage.class);
                    AppNameCache.appName.add(runLogMessage.getAppName());
                    if (runLogMessage.getLogLevel().toUpperCase().equals("ERROR")) {
                        errorLogs.add(runLogMessage);
                    }
                }
                logs=null;
                applicationEventPublisher.publishEvent(new PlumelogMonitorEvent(this, errorLogs));
            }catch (Exception e){
                logger.error("publisherMonitorEvent error!", e);
            }
        }

    }
}
