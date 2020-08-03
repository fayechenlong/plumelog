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
                if(logger.isDebugEnabled()){
                    logs.forEach(log->{
                        logger.debug(log);
                    });
                }
            } catch (Exception e) {
                logger.error("从plumelog-server拉取日志失败！", e);
            }
            //发布一个事件
            super.sendLog(super.getRunLogIndex(),logs);
            publisherMonitorEvent(logs);
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
                if(logger.isDebugEnabled()){
                    logs.forEach(log->{
                        logger.debug(log);
                    });
                }
            } catch (Exception e) {
                logger.error("从plumelog-server队列拉取日志失败！", e);
            }
            super.sendLog(super.getTraceLogIndex(),logs);
        }
    }

}
