package com.plumelog.server.collect;


import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.server.client.PlumeRestClient;
import com.plumelog.server.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * className：RedisLogCollect
 * description：RedisLogCollect 获取rest接口中日志，存储到es
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RestLogCollect extends BaseLogCollect {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(RestLogCollect.class);
    private String restUserName = "";
    private String restPassWord = "";
    private String restUrl = "";

    public RestLogCollect(ElasticLowerClient elasticLowerClient, ApplicationEventPublisher applicationEventPublisher) {

        this.restUserName = InitConfig.restUserName;
        this.restPassWord = InitConfig.restPassWord;
        this.restUrl = InitConfig.restUrl;
        super.elasticLowerClient = elasticLowerClient;
        super.applicationEventPublisher = applicationEventPublisher;
        logger.info("restUrl:{}", restUrl);
    }

    public void restStart() {

        threadPoolExecutor.execute(() -> {
            collectRuningLog();
        });
        threadPoolExecutor.execute(() -> {
            collectTraceLog();
        });
        logger.info("RestLogCollect is starting!");
    }

    private void collectRuningLog() {
        while (true) {
            List<String> logs = new ArrayList<>();
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                logs = PlumeRestClient.getLogs(this.restUrl + "?maxSendSize=" + InitConfig.MAX_SEND_SIZE + "&logKey=" + LogMessageConstant.LOG_KEY, this.restUserName, this.restPassWord);
            } catch (Exception e) {
                logger.error("从plumelog-server拉取日志失败！", e);
            }
            //发布一个事件
            publisherMonitorEvent(logs);
            collect(logs, LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));

        }
    }

    private void collectTraceLog() {
        while (true) {
            List<String> logs = new ArrayList<>();
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                logs = PlumeRestClient.getLogs(this.restUrl + "?maxSendSize=" + InitConfig.MAX_SEND_SIZE + "&logKey=" + LogMessageConstant.LOG_KEY_TRACE, this.restUserName, this.restPassWord);
            } catch (Exception e) {
                logger.error("从plumelog-server队列拉取日志失败！", e);
            }
            collectTrace(logs, LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
        }
    }

    private void collect(List<String> logs, String index) {
        if (logs.size() > 0) {
            logs.forEach(log -> {
                logger.debug("get log:" + log);
                super.logList.add(log);
            });
            if (super.logList.size() > 0) {
                List<String> logList = new ArrayList();
                logList.addAll(super.logList);
                super.logList.clear();
                super.sendLog(index, logList);
            }
        }
    }

    private void collectTrace(List<String> logs, String index) {
        if (logs.size() > 0) {
            logs.forEach(log -> {
                logger.debug("get log:" + log);
                super.traceLogList.add(log);
            });
            if (super.traceLogList.size() > 0) {
                List<String> logList = new ArrayList();
                logList.addAll(super.traceLogList);
                super.traceLogList.clear();
                super.sendTraceLogList(index, logList);
            }
        }
    }
}
