package com.beeplay.easylog.core.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * @ClassName LongEventProducerWithTranslator
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 20:15
 * @Version 1.0
 **/
public class LogMessageEventProducerWithTranslator {
    private final RingBuffer<LogMessageEvent> ringBuffer;

    public LogMessageEventProducerWithTranslator(RingBuffer<LogMessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<LogMessageEvent, ByteBuffer> TRANSLATOR =
            new EventTranslatorOneArg<LogMessageEvent, ByteBuffer>() {
                @Override
                public void translateTo(LogMessageEvent event, long sequence, ByteBuffer bb) {
                  //  event.set(bb.get(0));
                }
            };

    public void onData(ByteBuffer bb){
        ringBuffer.publishEvent(TRANSLATOR, bb);
    }
}
