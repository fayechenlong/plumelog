package com.plumelog.core.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * className：ThreadPoolUtil
 * description：带阻塞队列的线程池
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class ThreadPoolUtil {
    public static ThreadPoolExecutor getPool() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                10, 10, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        return threadPool;
    }

    public static ThreadPoolExecutor getPool(int corePoolSize, int maxPoolSize, int capacity) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(capacity),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        return threadPool;
    }
}
