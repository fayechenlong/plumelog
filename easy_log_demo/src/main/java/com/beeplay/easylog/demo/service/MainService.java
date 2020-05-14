package com.beeplay.easylog.demo.service;


import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.util.LogExceptionStackTrace;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import com.beeplay.easylog.demo.dubbo.service.EasyLogDubboService;
import com.beeplay.easylog.trace.annotation.Trace;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

@Service
public class MainService {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(MainService.class);
    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool(4, 8, 5000);
    @Autowired
    TankService tankService;

    @Reference
    EasyLogDubboService easyLogDubboService;

    @Trace
    public void testLog() {
        easyLogDubboService.testLogDubbo();
        System.out.println("testLog===>" + System.currentTimeMillis());
        try {
            logger.info("testLog===> 开始" + System.currentTimeMillis());
            say(System.currentTimeMillis() + "ppp");
            tankService.tankSay("ppp");
            logger.info("testLog===> 结束" + System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("{}", LogExceptionStackTrace.erroStackTrace(e));
        }
        threadPoolExecutor.execute(()->{

            TraceId.logTraceID.get();

            logger.info("我是子线程的日志1！{}",TraceId.logTraceID.get());
        });
    }

    public void say(String name) {
        System.out.println("say===>" + name);
    }
}
