package com.beeplay.easylog.core;

import com.beeplay.easylog.core.disruptor.LogEvent;
import com.beeplay.easylog.core.disruptor.LogMessageProducer;
import com.beeplay.easylog.core.disruptor.LogRingBuffer;
import com.beeplay.easylog.core.dto.BaseLogMessage;

/**
 * className：MessageAppenderFactory
 * description： TODO
 * time：2020-05-13.14:18
 *
 * @author Tank
 * @version 1.0.0
 */
public class MessageAppenderFactory {

    public static void push(String appName, BaseLogMessage baseLogMessage, AbstractClient client) {
        LogMessageProducer producer = new LogMessageProducer(LogRingBuffer.ringBuffer);
        LogEvent event = new LogEvent();
        event.setAppName(appName);
        event.setClient(client);
        event.setBaseLogMessage(baseLogMessage);
        producer.send(event);
    }

}
