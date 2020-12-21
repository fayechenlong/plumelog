package com.plumelog.server.collect;


import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.config.RedisClientFactory;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
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

    private RedisClientFactory redisClientFactory;

    public RedisLogCollect(ElasticLowerClient elasticLowerClient, RedisClientFactory redisClientFactory, ApplicationEventPublisher applicationEventPublisher) {
        super.elasticLowerClient = elasticLowerClient;
        this.redisClientFactory = redisClientFactory;
        super.applicationEventPublisher = applicationEventPublisher;
    }

    public void redisStart() {
        //todo 线程出现问题后， 会导致消费停止
        threadPoolExecutor.execute(() -> collectRuningLog());

        threadPoolExecutor.execute(() -> collectTraceLog());

        logger.info("RedisLogCollect is starting!");
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
                RedisClient redisClient = redisClientFactory.getRedisClient();
                long startTime=System.currentTimeMillis();
                logs = redisClient.getMessage(LogMessageConstant.LOG_KEY, InitConfig.MAX_SEND_SIZE);
                long endTime=System.currentTimeMillis();
                logger.info("RuningLog日志获取耗时：{}",endTime-startTime);
                // 设置权重
                redisClient.setWeight(logs.size());
                redisClient.setLatestPullTime(endTime);
                if(logger.isDebugEnabled()){
                logs.forEach(log->{
                    logger.debug(log);
                });
                }
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

            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                RedisClient redisClient = redisClientFactory.getRedisClient();
                long startTime=System.currentTimeMillis();
                logs = redisClientFactory.getRedisClient().getMessage(LogMessageConstant.LOG_KEY_TRACE, InitConfig.MAX_SEND_SIZE);
                long endTime=System.currentTimeMillis();
                logger.info("TraceLog日志获取耗时：{}",endTime-startTime);
                // 设置权重
                redisClient.setWeight(logs.size());
                redisClient.setLatestPullTime(endTime);
                if(logger.isDebugEnabled()){
                    logs.forEach(log->{
                        logger.debug(log);
                    });
                }
            } catch (LogQueueConnectException e) {
                logger.error("从redis队列拉取日志失败！", e);
            }
            super.sendTraceLogList(super.getTraceLogIndex(), logs);
        }
    }
}
