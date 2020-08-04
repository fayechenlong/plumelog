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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

    private static Boolean logOutPut = true;

    private static BlockingQueue<String> rundataQueue = new LinkedBlockingQueue<>(100000);
    private static BlockingQueue<String> tracedataQueue = new LinkedBlockingQueue<>(100000);

    /**
     * 当下游异常的时候，状态缓存时间
     */
    private final static Cache<String, Boolean> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS).build();

    /**
     * 设置阻塞队列为5000，防止OOM
     */
    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool(4, 8, 5000);

    public static void push(BaseLogMessage baseLogMessage) {
        LogMessageProducer producer = new LogMessageProducer(LogRingBuffer.ringBuffer);
        producer.send(baseLogMessage);
    }
    public static void pushRundataQueue(String message) {
        rundataQueue.add(message);
    }

    public static void pushTracedataQueue(String message) {
        tracedataQueue.add(message);
    }

    /**
     * 带队列宕机保护的push方法
     *
     * @param baseLogMessage
     * @param client
     * @param logOutPutKey
     */
//    public static void push(BaseLogMessage baseLogMessage, AbstractClient client, String logOutPutKey) {
//        final String redisKey =
//                baseLogMessage instanceof RunLogMessage
//                        ? LogMessageConstant.LOG_KEY
//                        : LogMessageConstant.LOG_KEY_TRACE;
//        threadPoolExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                logOutPut = cache.getIfPresent(logOutPutKey);
//                if (logOutPut == null || logOutPut) {
//                    try {
//                        client.pushMessage(redisKey, GfJsonUtil.toJSONString(baseLogMessage));
//                        //写入成功重置异常数量
//                        logOutPut = true;
//                        exceptionCount = 0;
//                        cache.put(logOutPutKey, true);
//                    } catch (LogQueueConnectException e) {
//                        exceptionCount++;
//                        if (exceptionCount > maxExceptionCount) {
//                            cache.put(logOutPutKey, false);
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        });
//    }

//    public static void push(String redisKey, String baseLogMessage, AbstractClient client, String logOutPutKey) {
//
//        threadPoolExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                logOutPut = cache.getIfPresent(logOutPutKey);
//                if (logOutPut == null || logOutPut) {
//                    try {
//                        client.pushMessage(redisKey, baseLogMessage);
//                        //写入成功重置异常数量
//                        logOutPut = true;
//                        exceptionCount = 0;
//                        cache.put(logOutPutKey, true);
//                    } catch (LogQueueConnectException e) {
//                        exceptionCount++;
//                        if (exceptionCount > maxExceptionCount) {
//                            cache.put(logOutPutKey, false);
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        });
//    }

    public static void push(String redisKey, List<String> baseLogMessage, AbstractClient client, String logOutPutKey) {
        logOutPut = cache.getIfPresent(logOutPutKey);
        if (logOutPut == null || logOutPut) {
            try {
                client.putMessageList(redisKey, baseLogMessage);
                cache.put(logOutPutKey, true);
            } catch (LogQueueConnectException e) {
                cache.put(logOutPutKey, false);
                e.printStackTrace();
            }
        }else {
            if(redisKey.equals(LogMessageConstant.LOG_KEY)){
                rundataQueue.addAll(baseLogMessage);
            }
            if(redisKey.equals(LogMessageConstant.LOG_KEY_TRACE)){
                tracedataQueue.addAll(baseLogMessage);
            }
        }

    }

    public static void startRunLog(AbstractClient client,int maxCount) {
        while (true) {
            try {
                List<String> logs = new ArrayList<>();
                int count=rundataQueue.drainTo(logs, maxCount);
                if(count>0) {
                    push(LogMessageConstant.LOG_KEY, logs, client, "plume.log.ack");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void startTraceLog(AbstractClient client,int maxCount) {
        while (true) {
            try {
                List<String> logs = new ArrayList<>();
                int count=tracedataQueue.drainTo(logs, maxCount);
                if(count>0) {
                    push(LogMessageConstant.LOG_KEY_TRACE, logs, client, "plume.log.ack");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
