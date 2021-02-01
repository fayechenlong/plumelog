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

    public static BlockingQueue<String> rundataQueue;
    public static BlockingQueue<String> tracedataQueue;

    public static int queueSize = 10000;

    /**
     * 当下游异常的时候，状态缓存时间
     */
    private final static Cache<String, Boolean> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS).build();


    public static void initQueue(int logQueueSize) {
        queueSize = logQueueSize;
        rundataQueue = new LinkedBlockingQueue<>(logQueueSize);
        tracedataQueue = new LinkedBlockingQueue<>(logQueueSize);
    }

    public static void push(BaseLogMessage baseLogMessage) {
        LogMessageProducer producer = new LogMessageProducer(LogRingBuffer.ringBuffer);
        producer.send(baseLogMessage);
    }

    public static void pushRundataQueue(String message) {
        if (message != null) {
            if (rundataQueue.size() < queueSize) {
                rundataQueue.add(message);
            }
        }
    }

    public static void pushTracedataQueue(String message) {
        if (message != null) {
            if (tracedataQueue.size() < queueSize) {
                tracedataQueue.add(message);
            }
        }
    }

    public static void pushRundataQueue(String message, int size) {
        if (message != null) {
            rundataQueue.add(message);
        }
    }

    public static void pushTracedataQueue(String message, int size) {
        if (message != null) {
            tracedataQueue.add(message);
        }
    }

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
        }

    }

    public static void startRunLog(AbstractClient client, int maxCount) {
        while (true) {
            try {
                List<String> logs = new ArrayList<>();
                int count = rundataQueue.drainTo(logs, maxCount);
                if (count > 0) {
                    push(LogMessageConstant.LOG_KEY, logs, client, "plume.log.ack");
                } else {
                    String log = rundataQueue.take();
                    logs.add(log);
                    push(LogMessageConstant.LOG_KEY, logs, client, "plume.log.ack");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {

                }
            }
        }

    }

    public static void startTraceLog(AbstractClient client, int maxCount) {
        while (true) {
            try {
                List<String> logs = new ArrayList<>();
                int count = tracedataQueue.drainTo(logs, maxCount);
                if (count > 0) {
                    push(LogMessageConstant.LOG_KEY_TRACE, logs, client, "plume.log.ack");
                } else {
                    String log = tracedataQueue.take();
                    logs.add(log);
                    push(LogMessageConstant.LOG_KEY_TRACE, logs, client, "plume.log.ack");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {

                }
            }
        }
    }
}
