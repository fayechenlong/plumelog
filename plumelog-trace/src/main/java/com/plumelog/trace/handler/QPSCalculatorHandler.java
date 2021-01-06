package com.plumelog.trace.handler;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.ClientConfig;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.util.DateUtil;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.dto.QPSLogMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class QPSCalculatorHandler extends AbstractQPSHandler {

    /**
     * 当下游异常的时候，暂存数据
     */
    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    /**
     * 接口URI
     */
    private String requestURI;

    /**
     * 槽位的数量
     */
    private int sizeOfBuckets;
    /**
     * 时间片（毫秒）
     */
    private int unitOfTimeSlice;
    /**
     * 最近一次push日志时间（毫秒）
     */
    private volatile Long latestPushTime;
    /**
     * push频率（毫秒）
     */
    private int pushTime;
    /**
     * push窗口大小（毫秒）
     */
    private int pushWindowTime;
    /**
     * 槽位
     */
    private Bucket[] buckets;
    /**
     * push日志的锁
     */
    private ReentrantLock enterNextBucketLock;

    /**
     * 默认4个槽位，槽位的时间片为 1 秒
     */
    public QPSCalculatorHandler(String requestURI) {
        this(4, 1000);
        this.requestURI = requestURI;
    }

    /**
     * 初始化Bucket数量与每个Bucket的时间片等
     *
     * @param sizeOfBuckets   桶数量
     * @param unitOfTimeSlice 桶时间长度
     */
    public QPSCalculatorHandler(int sizeOfBuckets, int unitOfTimeSlice) {
        this.latestPushTime = System.currentTimeMillis();
        this.sizeOfBuckets = sizeOfBuckets;
        this.unitOfTimeSlice = unitOfTimeSlice;
        this.pushTime = unitOfTimeSlice;
        this.pushWindowTime = unitOfTimeSlice * 2;
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
    public void record(Long totalTimeMillis) {
        threadPoolExecutor.execute(() -> {
            long passTime = System.currentTimeMillis();
            // 计算桶位置
            int targetBucketPosition = (int) (passTime / unitOfTimeSlice) % sizeOfBuckets;
            Bucket currentBucket = buckets[targetBucketPosition];
            // 计数 +1
            currentBucket.pass(passTime, totalTimeMillis);

            if (passTime - this.latestPushTime >= pushTime) {
                enterNextBucketLock.lock();
                try {
                    // 设置个 同步时间的变量
                    this.latestPushTime = passTime;

                    for (int i = 0; i < sizeOfBuckets; i++) {
                        // 大于 时限的 进行push
                        if (passTime - buckets[i].getLatestPassedTime() >= pushWindowTime) {
                            push(buckets[i]);
                        }
                    }
                } finally {
                    enterNextBucketLock.unlock();
                }
            }
        });
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
        long incr = bucket.countTotalPassed();
        qpsLogMessage.setIncr(incr);

        qpsLogMessage.setMaxTime(bucket.getMaxTime());
        qpsLogMessage.setMinTime(bucket.getMinTime());
        long sumTime = bucket.countSumTime();
        qpsLogMessage.setAvgTime(sumTime / incr);

        String dtTime = DateUtil.parseDateToStr(new Date(bucket.getLatestPassedTime()), DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS);
        qpsLogMessage.setDtTime(Long.valueOf(dtTime));
        String msg = GfJsonUtil.toJSONString(qpsLogMessage);
        list.add(msg);

        for (int i = 0; i < 500; i++) {
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
                queue.addAll(list);
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

        /**
         * 最大处理时间
         */
        private Long maxTime = 0L;

        /**
         * 最小处理时间
         */
        private Long minTime = 0L;

        /**
         * 请求处理时间合计
         */
        private LongAdder sumTime;


        public Bucket() {
            this.latestPassedTime = System.currentTimeMillis();
            this.longAdder = new LongAdder();
        }

        public void pass(Long passTime, Long totalTimeMillis) {
            longAdder.increment();
            this.latestPassedTime = passTime;
            // 计算耗时
            if (totalTimeMillis > maxTime) {
                maxTime = totalTimeMillis;
            }
            if (totalTimeMillis < minTime) {
                minTime = totalTimeMillis;
            }
            sumTime.add(totalTimeMillis);
        }

        public long countTotalPassed() {
            return longAdder.sum();
        }

        public long getLatestPassedTime() {
            return latestPassedTime;
        }

        public void reset() {
            // LongAdder的reset()性能很差，所以直接new新的对象
            this.longAdder = new LongAdder();
            this.latestPassedTime = System.currentTimeMillis();
        }

        public Long getMaxTime() {
            return maxTime;
        }

        public Long getMinTime() {
            return minTime;
        }

        public long countSumTime() {
            return sumTime.sum();
        }
    }


}
