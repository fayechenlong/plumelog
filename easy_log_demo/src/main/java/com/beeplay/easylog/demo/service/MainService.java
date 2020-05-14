package com.beeplay.easylog.demo.service;


import com.alibaba.ttl.threadpool.TtlExecutors;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.util.LogExceptionStackTrace;
import com.beeplay.easylog.demo.dubbo.service.EasyLogDubboService;
import com.beeplay.easylog.trace.annotation.Trace;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class MainService {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(MainService.class);
    private static ExecutorService executorService = TtlExecutors.getTtlExecutorService(
            new ThreadPoolExecutor(8, 8,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>()));
    @Autowired
    TankService tankService;

    @Reference
    EasyLogDubboService easyLogDubboService;

    @Trace
    public void testLog() {
        easyLogDubboService.testLogDubbo();
        System.out.println("testLog===>" + System.currentTimeMillis());
        try {
            logger.info(Thread.currentThread().getName() + "testLog===> 开始" + System.currentTimeMillis());
            say(System.currentTimeMillis() + "ppp");
            tankService.tankSay("ppp");
            logger.info(Thread.currentThread().getName() + "testLog===> 结束" + System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("{}", LogExceptionStackTrace.erroStackTrace(e));
        }
        executorService.execute(() -> {
            logger.info("testLog =》我是子线程的日志1！{}", TraceId.logTraceID.get());
        });
    }

    public void say(String name) {
        System.out.println("say===>" + name);
    }
}
