package com.plumelog.core.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.plumelog.core.dto.BaseLogMessage;

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

    public void send(BaseLogMessage data) {
        long next = ringBuffer.next();
        try {
            LogEvent event = ringBuffer.get(next);
            event.setBaseLogMessage(data);
        } finally {
            ringBuffer.publish(next);
        }
    }
}
