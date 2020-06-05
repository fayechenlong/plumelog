package com.plumelog.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.plumelog.core.disruptor.LogMessageProducer;
import com.plumelog.core.disruptor.LogRingBuffer;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * className：MessageAppenderFactory
 * description： TODO
 * time：2020-05-13.14:18
 *
 * @author Tank
 * @version 1.0.0
 */
public class MessageAppenderFactory {

    private static Boolean logOutPut=true;
    private static int exceptionCount=0;
    //异常超过maxExceptionCount不在输送日志到队列
    private final static int maxExceptionCount=10;
    //当下游异常的时候，状态缓存时间
    private final static Cache<String, Boolean>  cache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS).build();

    //设置阻塞队列为5000，防止OOM
    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool(4, 8, 5000);

    public static void push(BaseLogMessage baseLogMessage) {
        LogMessageProducer producer = new LogMessageProducer(LogRingBuffer.ringBuffer);
        producer.send(baseLogMessage);
    }
    public static void push(BaseLogMessage baseLogMessage, AbstractClient client) {
        final String redisKey =
                baseLogMessage instanceof RunLogMessage
                        ? LogMessageConstant.LOG_KEY
                        : LogMessageConstant.LOG_KEY_TRACE;
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.pushMessage(redisKey, GfJsonUtil.toJSONString(baseLogMessage));
                    } catch (LogQueueConnectException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    /**
     * 带队列宕机保护的push方法
     * @param baseLogMessage
     * @param client
     * @param logOutPutKey
     */
    public static void push(BaseLogMessage baseLogMessage, AbstractClient client, String logOutPutKey) {
        final String redisKey =
                baseLogMessage instanceof RunLogMessage
                        ? LogMessageConstant.LOG_KEY
                        : LogMessageConstant.LOG_KEY_TRACE;
        logOutPut=cache.getIfPresent(logOutPutKey);
        if(logOutPut==null||logOutPut) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.pushMessage(redisKey, GfJsonUtil.toJSONString(baseLogMessage));
                        //写入成功重置异常数量
                        logOutPut=true;
                        exceptionCount=0;
                        cache.put(logOutPutKey,true);
                    } catch (LogQueueConnectException e) {
                        exceptionCount++;
                        if(exceptionCount>maxExceptionCount){
                            cache.put(logOutPutKey,false);
                        }
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
