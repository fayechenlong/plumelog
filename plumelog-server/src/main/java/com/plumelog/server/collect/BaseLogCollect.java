package com.plumelog.server.collect;

import com.alibaba.fastjson.JSON;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.server.InitConfig;
import com.plumelog.server.cache.AppNameCache;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.server.monitor.PlumelogMonitorEvent;
import com.plumelog.server.util.IndexUtil;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：BaseLogCollect description：BaseLogCollect 基类
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

    public String getRunLogIndex() {
        if ("day".equals(InitConfig.ES_INDEX_MODEL)) {
            return IndexUtil.getRunLogIndex(System.currentTimeMillis());
        } else {
            return IndexUtil.getRunLogIndexWithHour(System.currentTimeMillis());
        }
    }

    public String getTraceLogIndex() {
        if ("day".equals(InitConfig.ES_INDEX_MODEL)) {
            return IndexUtil.getTraceLogIndex(System.currentTimeMillis());
        } else {
            return IndexUtil.getTraceLogIndexWithHour(System.currentTimeMillis());
        }
    }

    public void sendLog(String index, List<String> sendList) {
        try {
            if (sendList.size() > 0) {
                elasticLowerClient.insertListLog(sendList, index, LogMessageConstant.ES_TYPE);
            }
        } catch (Exception e) {
            logger.error("logList insert es failed!", e);
        }
    }

    public void sendTraceLogList(String index, List<String> sendTraceLogList) {
        try {
            if (sendTraceLogList.size() > 0) {
                elasticLowerClient.insertListTrace(sendTraceLogList, index, LogMessageConstant.ES_TYPE);
            }
        } catch (Exception e) {
            logger.error("traceLogList insert es failed!", e);
        }
    }

    protected void publisherMonitorEvent(List<String> logs) {
        int size = logs.size();
        if (size > 0) {
            try {
                List<RunLogMessage> errorLogs = new ArrayList<>();
                for (String logString : logs) {
                    RunLogMessage runLogMessage = JSON.parseObject(logString, RunLogMessage.class);
                    AppNameCache.appName.computeIfAbsent(runLogMessage.getAppName(), k -> new HashSet<>())
                            .add(runLogMessage.getEnv());
                    if ("ERROR".equals(runLogMessage.getLogLevel().toUpperCase())) {
                        errorLogs.add(runLogMessage);
                    }
                }
                logs = null;
                applicationEventPublisher.publishEvent(new PlumelogMonitorEvent(this, errorLogs));
            } catch (Exception e) {
                logger.error("publisherMonitorEvent error!", e);
            }
        }

    }
}
