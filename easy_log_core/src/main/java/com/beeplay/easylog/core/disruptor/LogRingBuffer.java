package com.beeplay.easylog.core.disruptor;

import com.lmax.disruptor.*;

import java.util.concurrent.Executors;

/**
 * className：LogRingBuffer
 * description： TODO
 * time：2020-05-19.15:26
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogRingBuffer {

    public static RingBuffer<LogEvent> ringBuffer = getRingBuffer();


    private static RingBuffer<LogEvent> getRingBuffer() {

        RingBuffer<LogEvent> ringBuffer =
                RingBuffer.createMultiProducer(
                        new EventFactory<LogEvent>() {
                            public LogEvent newInstance() {
                                return new LogEvent();
                            }
                        }, 512, new YieldingWaitStrategy());
        //2 通过ringBuffer 创建一个屏障
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        //3 创建含有10个消费者的数组:
        LogMessageConsumer[] consumers = new LogMessageConsumer[10];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new LogMessageConsumer("C" + i);
        }
        //4 构建多消费者工作池
        WorkerPool<LogEvent> workerPool = new WorkerPool<LogEvent>(
                ringBuffer,
                sequenceBarrier,
                new EventExceptionHandler(),
                consumers);
        //5 设置多个消费者的sequence序号 用于单独统计消费进度, 并且设置到ringbuffer中
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        //6 启动workerPool
        workerPool
                .start(Executors.newFixedThreadPool(10));
        return ringBuffer;
    }
}
