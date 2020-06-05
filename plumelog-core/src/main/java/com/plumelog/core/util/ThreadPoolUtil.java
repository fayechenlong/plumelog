package com.plumelog.core.util;

import java.util.concurrent.*;
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
                10,10,10,TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        return threadPool;
    }
    public static ThreadPoolExecutor getPool(int corePoolSize,int maxPoolSize,int capacity) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                corePoolSize,maxPoolSize,10,TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(capacity),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        return threadPool;
    }
}
