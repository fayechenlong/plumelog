package com.plumelog.trace.handler;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.ClientConfig;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.util.DateUtil;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.dto.QPSLogMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class QPSCalculatorHandler {

    /**
     * 当下游异常的时候，暂存数据，最大5000条
     */
    private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();

    /**
     * 接口URI
     */
    private String requestURI;

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
    public QPSCalculatorHandler(String requestURI) {
        this(10, 1000);
        this.requestURI = requestURI;
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
                        push(buckets[i]);
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
     */
    private void push(Bucket bucket) {

        List<String> list = new ArrayList<>();

        QPSLogMessage qpsLogMessage = new QPSLogMessage();
        qpsLogMessage.setMessageId(String.valueOf(bucket.getLatestPassedTime()));
        qpsLogMessage.setNamespace(ClientConfig.getNameSpance());
        qpsLogMessage.setAppName(ClientConfig.getAppName());
        qpsLogMessage.setServerName(ClientConfig.getServerName());
        qpsLogMessage.setRequestURI(requestURI);
        qpsLogMessage.setIncr(bucket.countTotalPassed());
        String dtTime = DateUtil.parseDateToStr(new Date(bucket.getLatestPassedTime()), DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS);
        qpsLogMessage.setDtTime(dtTime);
        String msg = GfJsonUtil.toJSONString(qpsLogMessage);
        list.add(msg);

        for (int i = 0; i < 100; i++) {
            if (queue.isEmpty()) {
                break;
            }
            list.add(queue.poll());
        }

        try {
            AbstractClient.getClient().putMessageList(LogMessageConstant.QPS_KEY, list);
        } catch (LogQueueConnectException e) {
            // 发送失败后 进行暂存数据
            if (queue.size() < 5000) {
                queue.add(msg);
            }
            e.printStackTrace();
        }
        // 重置
        bucket.reset();
    }

    public void shutdown() {

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
            longAdder.increment();
            this.latestPassedTime = passTime;
        }

        public long countTotalPassed() {
            return longAdder.sum();
        }

        public long getLatestPassedTime() {
            return latestPassedTime;
        }

        public void reset() {
            this.longAdder = new LongAdder();
            this.latestPassedTime = System.currentTimeMillis();
        }
    }


}
