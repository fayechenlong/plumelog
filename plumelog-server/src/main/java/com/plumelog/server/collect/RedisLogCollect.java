package com.plumelog.server.collect;


import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.redis.RedisClientService;
import com.plumelog.core.redis.RedisLogCollectService;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.redis.RedisClientFactory;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * className：RedisLogCollect
 * description：RedisLogCollect 获取redis中日志，存储到es
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisLogCollect extends BaseLogCollect implements RedisLogCollectService {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(RedisLogCollect.class);

    private RedisClientFactory redisClientFactory;

    public RedisLogCollect(ElasticLowerClient elasticLowerClient, RedisClientFactory redisClientFactory, ApplicationEventPublisher applicationEventPublisher) {
        super.elasticLowerClient = elasticLowerClient;
        this.redisClientFactory = redisClientFactory;
        super.applicationEventPublisher = applicationEventPublisher;
    }

    public void redisStart() {
        scheduled.scheduleWithFixedDelay(() -> collectRuningLog(), 1, InitConfig.MAX_INTERVAL, TimeUnit.MILLISECONDS);
        scheduled.scheduleWithFixedDelay(() -> collectTraceLog(), 1, InitConfig.MAX_INTERVAL, TimeUnit.MILLISECONDS);
        scheduled.scheduleWithFixedDelay(() -> collectQPSLog(), 1, InitConfig.MAX_INTERVAL, TimeUnit.MILLISECONDS);
        logger.info("RedisLogCollect is starting!");
    }

    private void collectRuningLog() {
        List<String> logs = new ArrayList<>();

        try {
            RedisClientService redisClient = redisClientFactory.getRedisClient();
            if (redisClient == null) {
                return;
            }
            long startTime = System.currentTimeMillis();
            logs = redisClient.getMessage(LogMessageConstant.LOG_KEY, InitConfig.MAX_SEND_SIZE);
            long endTime = System.currentTimeMillis();
            logger.info("RuningLog日志获取耗时：{}", endTime - startTime);
            if (logger.isDebugEnabled()) {
                logs.forEach(log -> {
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

    private void collectTraceLog() {
        List<String> logs = new ArrayList<>();

        try {
            RedisClientService redisClient = redisClientFactory.getRedisClient();
            long startTime = System.currentTimeMillis();
            logs = redisClientFactory.getRedisClient().getMessage(LogMessageConstant.LOG_KEY_TRACE, InitConfig.MAX_SEND_SIZE);
            long endTime = System.currentTimeMillis();
            logger.info("TraceLog日志获取耗时：{}", endTime - startTime);
            if (logger.isDebugEnabled()) {
                logs.forEach(log -> {
                    logger.debug(log);
                });
            }
        } catch (LogQueueConnectException e) {
            logger.error("从redis队列拉取日志失败！", e);
        }
        super.sendTraceLogList(super.getTraceLogIndex(), logs);
    }

    private void collectQPSLog() {
        List<String> logs = new ArrayList<>();

        try {
            RedisClientService redisClient = redisClientFactory.getRedisClient();
            long startTime = System.currentTimeMillis();
            logs = redisClient.getMessage(LogMessageConstant.QPS_KEY, InitConfig.MAX_SEND_SIZE);
            long endTime = System.currentTimeMillis();
            logger.info("RuningQPS日志获取耗时：{}", endTime - startTime);
            if (logger.isDebugEnabled()) {
                logs.forEach(log -> {
                    logger.debug(log);
                });
            }
        } catch (LogQueueConnectException e) {
            logger.error("从redis队列拉取QPS日志失败！", e);
        }
        super.sendQPSLogList(super.getQPSIndex(), logs);
    }

    /**
     * redis 取消注册后 消费剩余的日志
     * @param redisClient
     */
    @Override
    public void finallyCollect(RedisClientService redisClient) {

        boolean logFlag = true;
        boolean traceFlag = true;
        boolean qpsFlag = true;

        while (logFlag) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                long startTime = System.currentTimeMillis();
                List<String> logs = redisClient.getMessage(LogMessageConstant.LOG_KEY, 1000);
                long endTime = System.currentTimeMillis();
                logger.info("下线消费-RuningLog日志获取耗时：{}", endTime - startTime);
                redisClient.setLatestPullTime(endTime);

                super.sendLog(super.getRunLogIndex(), logs);
                //发布一个事件
                publisherMonitorEvent(logs);

                if (logs.size() == 0) {
                    logFlag = false;
                }

            } catch (LogQueueConnectException e) {
                logger.error("下线消费-从redis队列拉取日志失败！", e);
            }
        }

        while (traceFlag) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                long startTime = System.currentTimeMillis();
                List<String> logs = redisClientFactory.getRedisClient().getMessage(LogMessageConstant.LOG_KEY_TRACE, 1000);
                long endTime = System.currentTimeMillis();
                logger.info("下线消费-TraceLog日志获取耗时：{}", endTime - startTime);

                super.sendTraceLogList(super.getTraceLogIndex(), logs);

                if (logs.size() == 0) {
                    traceFlag = false;
                }
            } catch (LogQueueConnectException e) {
                logger.error("下线消费-从redis队列拉取日志失败！", e);
            }
        }

        while (qpsFlag) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                long startTime = System.currentTimeMillis();
                List<String> logs = redisClient.getMessage(LogMessageConstant.QPS_KEY, 1000);
                long endTime = System.currentTimeMillis();
                logger.info("下线消费-RuningQPS日志获取耗时：{}", endTime - startTime);

                super.sendQPSLogList(super.getQPSIndex(), logs);

                if (logs.size() == 0) {
                    qpsFlag = false;
                }

            } catch (LogQueueConnectException e) {
                logger.error("下线消费-从redis队列拉取QPS日志失败！", e);
            }
        }


    }




}
