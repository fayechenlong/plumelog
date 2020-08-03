package com.plumelog.server.collect;


import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * className：RedisLogCollect
 * description：RedisLogCollect 获取redis中日志，存储到es
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisLogCollect extends BaseLogCollect {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(RedisLogCollect.class);
    private RedisClient redisClient;

    public RedisLogCollect(ElasticLowerClient elasticLowerClient, RedisClient redisClient, ApplicationEventPublisher applicationEventPublisher) {
        super.elasticLowerClient = elasticLowerClient;
        this.redisClient = redisClient;
        super.applicationEventPublisher = applicationEventPublisher;
    }

    public void redisStart() {

        threadPoolExecutor.execute(() -> {
            collectRuningLog();
        });
        threadPoolExecutor.execute(() -> {
            collectTraceLog();
        });
        logger.info("RedisLogCollect is starting!");
    }

    private void collectRuningLog() {
        while (true) {
            List<String> logs = new ArrayList<>();
            if(logger.isDebugEnabled()){
                logs.forEach(log->{
                    logger.debug(log);
                });
            }
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                logs = redisClient.getMessage(LogMessageConstant.LOG_KEY, InitConfig.MAX_SEND_SIZE);
            } catch (LogQueueConnectException e) {
                logger.error("从redis队列拉取日志失败！", e);
            }
            super.sendLog(super.getRunLogIndex(), logs);
            //发布一个事件
            publisherMonitorEvent(logs);
        }
    }
    private void collectTraceLog() {
        while (true) {
            List<String> logs = new ArrayList<>();
            if(logger.isDebugEnabled()){
                logs.forEach(log->{
                    logger.debug(log);
                });
            }
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                logs = redisClient.getMessage(LogMessageConstant.LOG_KEY_TRACE, InitConfig.MAX_SEND_SIZE);
            } catch (LogQueueConnectException e) {
                logger.error("从redis队列拉取日志失败！", e);
            }
            super.sendTraceLogList(super.getTraceLogIndex(), logs);
        }
    }
}
