package com.beeplay.easylog.core.disruptor;

import com.lmax.disruptor.RingBuffer;

/**
 * className：LogMessageProducer
 * description： 日志生产
 * time：2020-05-19.13:52
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogMessageProducer {


    private RingBuffer<LogEvent> ringBuffer;

    public LogMessageProducer(RingBuffer<LogEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void send(LogEvent data) {
        long next = ringBuffer.next();
        System.out.println(next);
        try {
            LogEvent event = ringBuffer.get(next);
            event.setAppName(data.getAppName());
            event.setBaseLogMessage(data.getBaseLogMessage());
            event.setClient(data.getClient());
        } finally {
            ringBuffer.publish(next);
        }
    }
}
