package com.plumelog.lite.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.plumelog.core.TraceMessage;
import com.plumelog.core.client.AbstractServerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogCompressMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;
import com.plumelog.core.lucene.LuceneClient;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.LZ4Util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * className：MessageAppenderFactory
 * description： TODO
 * time：2020-05-13.14:18
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogSave {

    /**
     * 当下游异常的时候，状态缓存时间
     */
    private final static Cache<String, Boolean> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS).build();
    public static BlockingQueue<RunLogMessage> rundataQueue;
    public static BlockingQueue<TraceLogMessage> tracedataQueue;
    public static int queueSize = 10000;
    private static Boolean logOutPut = true;
    private static AtomicLong lastRunPushTime = new AtomicLong(0);
    private static AtomicLong lastTracePushTime = new AtomicLong(0);

    private static LuceneClient luceneClient;

    public static void init(int logQueueSize, String logPath) {
        queueSize = logQueueSize;
        if (rundataQueue == null) {
            rundataQueue = new LinkedBlockingQueue<>(logQueueSize);
        }
        if (tracedataQueue == null) {
            tracedataQueue = new LinkedBlockingQueue<>(logQueueSize);
        }
        if (luceneClient == null) {
            luceneClient = new LuceneClient(logPath);
        }
        InitConfig.LITE_MODE_LOG_PATH = logPath;
    }


    public static void pushRundataQueue(RunLogMessage message) {
        if (message != null) {
            if (rundataQueue.size() < queueSize) {
                rundataQueue.add(message);
            }
        }
    }

    public static void pushTracedataQueue(TraceLogMessage message) {
        if (message != null) {
            if (tracedataQueue.size() < queueSize) {
                tracedataQueue.add(message);
            }
        }
    }

    private static void push(String key, List<RunLogMessage> baseLogMessage, String logOutPutKey) {
        if (baseLogMessage.size() == 0) {
            return;
        }
        logOutPut = cache.getIfPresent(logOutPutKey);
        if (logOutPut == null || logOutPut) {
            try {
                if (key.equals(LogMessageConstant.LOG_KEY)) {
                    luceneClient.insertListLog(baseLogMessage, getRunLogIndex());
                }
                cache.put(logOutPutKey, true);
            } catch (Exception e) {
                cache.put(logOutPutKey, false);
                e.printStackTrace();
            }
        }
    }
    private static void pushTrace(String key, List<TraceLogMessage> baseLogMessage, String logOutPutKey) {
        if (baseLogMessage.size() == 0) {
            return;
        }
        logOutPut = cache.getIfPresent(logOutPutKey);
        if (logOutPut == null || logOutPut) {
            try {
                if (key.equals(LogMessageConstant.LOG_KEY_TRACE)) {
                    luceneClient.insertListTrace(baseLogMessage, getTraceLogIndex());
                }
                cache.put(logOutPutKey, true);
            } catch (Exception e) {
                cache.put(logOutPutKey, false);
                e.printStackTrace();
            }
        }
    }
    private static List<String> compress(List<String> baseLogMessage, boolean compress) {

        if (!compress) {
            return baseLogMessage;
        }
        String text = GfJsonUtil.toJSONString(baseLogMessage);
        byte[] textByte = text.getBytes(StandardCharsets.UTF_8);
        byte[] compressedByte = LZ4Util.compressedByte(textByte);
        RunLogCompressMessage message = new RunLogCompressMessage();
        message.setBody(compressedByte);
        message.setLength(textByte.length);
        return Lists.newArrayList(GfJsonUtil.toJSONString(message));
    }


    public static void startRunLog(int maxCount, String key, boolean compress) {
        while (true) {
            try {
                doStartLog(maxCount, rundataQueue, key, "plume.log.ack", lastRunPushTime);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                }
            }
        }
    }


    public static void startTraceLog(int maxCount, String key, boolean compress) {
        while (true) {
            try {
                doStartLogTrace(maxCount, tracedataQueue, key, "plume.log.ack", lastTracePushTime);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {

                }
            }
        }
    }

    private static void doStartLog(int maxCount, BlockingQueue<RunLogMessage> queue, String key, String lock, AtomicLong pushTime) throws InterruptedException {

        List<RunLogMessage> logs = new ArrayList<>();

        int size = queue.size();
        long currentTimeMillis = System.currentTimeMillis();
        long time = currentTimeMillis - pushTime.get();

        if (size >= maxCount || time > 500) {
            queue.drainTo(logs, maxCount);

            push(key, logs, lock);

            pushTime.set(currentTimeMillis);
        } else if (size == 0) {
            RunLogMessage log = queue.take();
            logs.add(log);

            push(key, logs, lock);

            pushTime.set(currentTimeMillis);
        } else {
            Thread.sleep(100);
        }
    }


    private static void doStartLogTrace(int maxCount, BlockingQueue<TraceLogMessage> queue, String key, String lock, AtomicLong pushTime) throws InterruptedException {

        List<TraceLogMessage> logs = new ArrayList<>();

        int size = queue.size();
        long currentTimeMillis = System.currentTimeMillis();
        long time = currentTimeMillis - pushTime.get();

        if (size >= maxCount || time > 500) {
            queue.drainTo(logs, maxCount);

            pushTrace(key, logs, lock);

            pushTime.set(currentTimeMillis);
        } else if (size == 0) {
            TraceLogMessage log = queue.take();
            logs.add(log);

            pushTrace(key, logs, lock);

            pushTime.set(currentTimeMillis);
        } else {
            Thread.sleep(100);
        }
    }

    private static String getRunLogIndex() {
        if ("day".equals(InitConfig.ES_INDEX_MODEL)) {
            return IndexUtil.getRunLogIndex(System.currentTimeMillis());
        } else {
            return IndexUtil.getRunLogIndexWithHour(System.currentTimeMillis());
        }
    }

    private static String getTraceLogIndex() {
        if ("day".equals(InitConfig.ES_INDEX_MODEL)) {
            return IndexUtil.getTraceLogIndex(System.currentTimeMillis());
        } else {
            return IndexUtil.getTraceLogIndexWithHour(System.currentTimeMillis());
        }
    }
}
