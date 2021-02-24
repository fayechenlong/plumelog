package com.plumelog.server.collect;


import com.alibaba.fastjson.JSON;
import com.plumelog.core.dto.RunLogCompressMessage;
import com.plumelog.core.AbstractClient;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.LZ4Util;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.core.constant.LogMessageConstant;
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
    private AbstractClient client;
    private boolean compressor;

    public RedisLogCollect(ElasticLowerClient elasticLowerClient, AbstractClient client, ApplicationEventPublisher applicationEventPublisher,boolean compressor) {
        super.elasticLowerClient = elasticLowerClient;
        this.client = client;
        super.applicationEventPublisher = applicationEventPublisher;
        this.compressor = compressor;
    }

    public void redisStart() {

        threadPoolExecutor.execute(() -> collectRuningLog(compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY));
        threadPoolExecutor.execute(() -> collectTraceLog(compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE));
        logger.info("RedisLogCollect is starting!");
    }

    private void collectRuningLog(String logKey) {
        while (true) {
            List<String> logs = new ArrayList<>();

            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                long startTime=System.currentTimeMillis();
                logs = client.getMessage(logKey, InitConfig.MAX_SEND_SIZE);
                long endTime=System.currentTimeMillis();
                if(logs.size() > 0) {
                    logger.info("RuningLog日志获取耗时：{}",endTime-startTime);
                    if(logger.isDebugEnabled()){
                        logs.forEach(log-> logger.debug(log));
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
            List<String> logs = new ArrayList<>();

            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                long startTime=System.currentTimeMillis();
                logs = client.getMessage(logKey, InitConfig.MAX_SEND_SIZE);
                long endTime=System.currentTimeMillis();
                if(logs.size()>0) {
                    logger.info("TraceLog日志获取耗时：{}",endTime-startTime);
                    if(logger.isDebugEnabled()){
                        logs.forEach(log-> logger.debug(log));
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

        List<String> list = new ArrayList<>();
        if (logs != null && logs.size() > 0) {
            logs.forEach(r -> {
                try {
                    RunLogCompressMessage message = JSON.parseObject(r, RunLogCompressMessage.class);
                    byte[] bytes = LZ4Util.decompressorByte(message.getBody(), message.getLength());
                    String json = new String(bytes);
                    list.addAll(GfJsonUtil.parseArray(json, String.class));
                } catch (Exception e) {
                    logger.error("解析日志失败！", e);
                }
            });
        }
        return list;
    }
}
