package com.beeplay.easylog.util;

import java.util.concurrent.*;

public class ThreadPoolUtil {
    public static ThreadPoolExecutor getPool() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                10,10,10,TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        return threadPool;
    }
}
