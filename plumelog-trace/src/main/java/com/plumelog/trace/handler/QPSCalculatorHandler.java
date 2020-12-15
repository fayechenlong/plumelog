package com.plumelog.trace.handler;

import java.io.Serializable;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class QPSCalculatorHandler {

    /**
     * 槽位的数量
     */
    private int sizeOfBuckets;
    /**
     * 时间片，单位毫秒
     */
    private int unitOfTimeSlice;
    /**
     * 最近一次push时间
     */
    private volatile Long latestPassedTimeClose;
    /**
     * 槽位
     */
    private Bucket[] buckets;
    /**
     * 进入下一个槽位时使用的锁
     */
    private ReentrantLock enterNextBucketLock;

    /**
     * 默认10个槽位，槽位的时间片为 1 秒
     */
    public QPSCalculatorHandler() {
        //todo 配置采样率问题，
        this(10, 1000);
    }

    /**
     * 初始化Bucket数量与每个Bucket的时间片等
     *
     * @param sizeOfBuckets   桶数量
     * @param unitOfTimeSlice 桶时间长度
     */
    public QPSCalculatorHandler(int sizeOfBuckets, int unitOfTimeSlice) {
        this.latestPassedTimeClose = System.currentTimeMillis();
        this.sizeOfBuckets = sizeOfBuckets;
        this.unitOfTimeSlice = unitOfTimeSlice;
        this.enterNextBucketLock = new ReentrantLock();
        this.buckets = new Bucket[sizeOfBuckets];
        // 初始化桶
        for (int i = 0; i < sizeOfBuckets; i++) {
            this.buckets[i] = new Bucket();
        }
    }

    /**
     * 计数 +1
     * 取模 不同的桶
     * 进行累计
     */
    public void record() {
        long passTime = System.currentTimeMillis();
        // 计算桶位置
        int targetBucketPosition = (int) (passTime / unitOfTimeSlice) % sizeOfBuckets;
        Bucket currentBucket = buckets[targetBucketPosition];
        // 计数 +1
        currentBucket.pass(passTime);

        if (passTime - this.latestPassedTimeClose >= unitOfTimeSlice) {
            enterNextBucketLock.lock();
            try {
                // 大于 桶数 * 桶时间 的 桶 全部进行push 日志，然后init 桶
                //todo 会丢失maxTime时长的统计日志
                long maxTime = sizeOfBuckets / 2 * unitOfTimeSlice;

                // 设置个 同步时间的变量
                this.latestPassedTimeClose = passTime;

                for (int i = 0; i < sizeOfBuckets; i++) {
                    // 大于 时限的 进行push
                    if (passTime - buckets[i].getLatestPassedTime() >= maxTime) {
                        push(buckets[i], passTime);
                    }
                }
            } finally {
                enterNextBucketLock.unlock();
            }
        }
    }

    /**
     * push 记录
     *
     * @param bucket
     * @param passTime
     */
    private void push(Bucket bucket, Long passTime) {
        //todo push到日志系统

        bucket.countTotalPassed();

        bucket.reset(passTime);
    }

    /**
     * 时间桶
     */
    private class Bucket implements Serializable {
        private static final long serialVersionUID = -9085720164508215774L;

        /**
         * 桶最后计数时间
         */
        private Long latestPassedTime;

        /**
         * 并发计数器
         */
        private LongAdder longAdder;

        public Bucket() {
            this.latestPassedTime = System.currentTimeMillis();
            this.longAdder = new LongAdder();
        }

        public void pass(Long passTime) {
            longAdder.add(1);
            this.latestPassedTime = passTime;
        }

        public long countTotalPassed() {
            return longAdder.sum();
        }

        public long getLatestPassedTime() {
            return latestPassedTime;
        }

        public void reset(long latestPassedTime) {
            this.longAdder = new LongAdder();
            this.latestPassedTime = latestPassedTime;
        }
    }
}
