package com.beeplay.easylog.demo.service;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import com.beeplay.easylog.trace.annotation.Trace;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * className：TankService
 * description：
 * time：2020-05-11.14:06
 *
 * @author Tank
 * @version 1.0.0
 */
@Service
public class TankService {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(TankService.class);
    @Autowired
    TankServiceTwo tankServiceTwo;

    @Autowired
    TankServiceThere tankServiceThere;

    @Trace
    public void tankSay(String data) {
        logger.info("tankSay==>>{}",data);
        tankServiceTwo.tankServiceTwo(data);
        tankServiceThere.tankServiceThere(data);
    }
}
