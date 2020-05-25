package com.beeplay.easylog.core;

import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.disruptor.LogEvent;
import com.beeplay.easylog.core.disruptor.LogMessageProducer;
import com.beeplay.easylog.core.disruptor.LogRingBuffer;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.dto.RunLogMessage;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.core.util.ThreadPoolUtil;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：MessageAppenderFactory
 * description： TODO
 * time：2020-05-13.14:18
 *
 * @author Tank
 * @version 1.0.0
 */
public class MessageAppenderFactory {

    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool(4, 8, 5000);


    /**
     * disruptor 的写入
     * @param appName
     * @param baseLogMessage
     * @param client
     */
    public static void push(String appName, BaseLogMessage baseLogMessage, AbstractClient client) {
//        LogMessageProducer producer = new LogMessageProducer(LogRingBuffer.ringBuffer);
//        LogEvent event = new LogEvent();
//        event.setAppName(appName);
//        event.setClient(client);
//        event.setBaseLogMessage(baseLogMessage);
//        producer.send(event);
    }

    /**
     * 直接使用线程池异步写入
     * @param baseLogMessage
     * @param client
     */
    public static void push(BaseLogMessage baseLogMessage, AbstractClient client) {
        final String redisKey =
                baseLogMessage instanceof RunLogMessage
                        ? LogMessageConstant.LOG_KEY
                        : LogMessageConstant.LOG_KEY_TRACE;
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                client.pushMessage(redisKey, GfJsonUtil.toJSONString(baseLogMessage));
            }
        });
    }

}
