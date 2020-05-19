package com.beeplay.easylog.core.disruptor;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * @ClassName LongEventProducer
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 20:14
 * @Version 1.0
 **/
public class LogMessageEventProducer {
    private final RingBuffer<LogMessageEvent> ringBuffer;

    public LogMessageEventProducer(RingBuffer<LogMessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    //发布事件
    public void onData(ByteBuffer bb){
        long sequence = ringBuffer.next();              //1.获取下一个序列
        try {
            LogMessageEvent event = ringBuffer.get(sequence); //2.获取disruptor的sequence
            event.set(bb.toString());             //3.设置事件Context
        }finally {
            ringBuffer.publish(sequence);               //4.发布事件
        }
    }
}
