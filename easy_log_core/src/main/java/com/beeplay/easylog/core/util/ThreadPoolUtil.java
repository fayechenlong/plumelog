package com.beeplay.easylog.core.util;

import java.util.concurrent.*;

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
                10,10,10,TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        return threadPool;
    }
}
