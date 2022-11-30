package com.plumelog.server.collect;

import com.alibaba.fastjson.JSON;
import com.plumelog.core.client.AbstractClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogCompressMessage;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.LZ4Util;
import com.plumelog.server.config.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
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
public class RedisLogCollect extends BaseLogCollect {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisLogCollect.class);
    private AbstractClient redisQueueClient;
    private boolean compressor;

    public RedisLogCollect(ElasticLowerClient elasticLowerClient, AbstractClient redisQueueClient, ApplicationEventPublisher applicationEventPublisher, boolean compressor,AbstractClient redisClient) {
        super.elasticLowerClient = elasticLowerClient;
        this.redisQueueClient = redisQueueClient;
        super.redisClient=redisClient;
        super.applicationEventPublisher = applicationEventPublisher;
        this.compressor = compressor;
    }

    public void redisStart() {
        Thread runLogThread = startRunLogThread();
        Thread traceLogThread = startTraceLogThread();

        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            Thread runLog = runLogThread;
            try {
                boolean runLogThreadAlive = runLog.isAlive();
                if (!runLogThreadAlive) {
                    throw new NullPointerException("runLogThread alive false");
                }
            } catch (Exception ex) {
                System.out.println("runLogThread 重启线程");
                runLog = startRunLogThread();
            }

            Thread traceLog = traceLogThread;
            try {
                boolean traceLogThreadAlive = traceLog.isAlive();
                if (!traceLogThreadAlive) {
                    throw new NullPointerException("traceLogThread alive false");
                }
            } catch (Exception ex) {
                logger.warn("traceLogThread 重启线程");
                traceLog = startTraceLogThread();
            }
        }, 10, 30, TimeUnit.SECONDS);

        logger.info("RedisLogCollect is starting!");
    }

    private Thread startRunLogThread() {
        Thread runLogThread = new Thread(() -> collectRunningLog(compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY));
        runLogThread.start();
        return runLogThread;
    }

    private Thread startTraceLogThread() {
        Thread traceLogThread = new Thread(() -> collectTraceLog(compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE));
        traceLogThread.start();
        return traceLogThread;
    }


    private void collectRunningLog(String logKey) {
        while (true) {
            List<String> logs = new ArrayList<>(InitConfig.MAX_SEND_SIZE);
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
                Thread.currentThread().interrupt();
            }
            try {
                long startTime = System.currentTimeMillis();
                logs = redisQueueClient.getMessage(logKey, InitConfig.MAX_SEND_SIZE);
                int size = logs.size();
                long endTime = System.currentTimeMillis();
                if (size > 0) {
                    logger.info("RunLog日志获取耗时：{} 日志条数：{}", endTime - startTime, size);
                    if (logger.isDebugEnabled()) {
                        logs.forEach(log -> logger.debug(log));
                    }
                    // 解压缩
                    logs = decompressor(logs);

                    super.sendLog(super.getRunLogIndex(), logs);
                    //发布一个事件
                    publisherMonitorEvent(logs);



                }
            } catch (LogQueueConnectException e) {
                logger.error("从redis队列拉取日志失败！", e);
            }
        }
    }

    private void collectTraceLog(String logKey) {
        while (true) {
            List<String> logs = new ArrayList<>(InitConfig.MAX_SEND_SIZE);
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
                Thread.currentThread().interrupt();
            }
            try {
                long startTime = System.currentTimeMillis();
                logs = redisQueueClient.getMessage(logKey, InitConfig.MAX_SEND_SIZE);
                long endTime = System.currentTimeMillis();
                int size = logs.size();
                if (size > 0) {
                    logger.info("TraceLog日志获取耗时：{} 日志条数：{}", endTime - startTime, size);
                    if (logger.isDebugEnabled()) {
                        logs.forEach(log -> logger.debug(log));
                    }
                    // 解压缩
                    logs = decompressor(logs);

                    super.sendTraceLogList(super.getTraceLogIndex(), logs);
                }
            } catch (LogQueueConnectException e) {
                logger.error("从redis队列拉取日志失败！", e);
            }
        }
    }

    private List<String> decompressor(List<String> logs) {
        if (!compressor) {
            return logs;
        }
        int size = logs.size();
        List<String> list = new ArrayList<>();
        if (logs != null && logs.size() > 0) {
            for (int i = 0; i < logs.size(); i++) {
                String r = logs.get(i);
                try {
                    RunLogCompressMessage message = JSON.parseObject(r, RunLogCompressMessage.class);
                    byte[] bytes = LZ4Util.decompressorByte(message.getBody(), message.getLength());
                    String json = new String(bytes);
                    list.addAll(GfJsonUtil.parseArray(json, String.class));
                } catch (Exception e) {
                    logger.error("解析日志失败！", e);
                }
            }
        }
        return list;
    }
}
