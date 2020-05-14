package com.beeplay.easylog.demo.service;

import com.alibaba.ttl.TtlRunnable;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import com.beeplay.easylog.trace.annotation.Trace;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

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
<<<<<<< HEAD
    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool(4, 8, 5000);
=======
>>>>>>> f5dd40435e65e96099cc87880c6db7106c53e41f
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(TankService.class);
    @Autowired
    TankServiceTwo tankServiceTwo;

    @Autowired
    TankServiceThere tankServiceThere;

    @Trace
<<<<<<< HEAD
    public void tankSay(String kk) {
        System.out.println("tankSay========>" + kk);
        tankServiceTwo.tankServiceTwo();
        tankServiceThere.tankServiceThere();

        threadPoolExecutor.execute(TtlRunnable.get(()->{
            TraceId.logTraceID.get();
            logger.info("tankSay =》我是子线程的日志！{}",TraceId.logTraceID.get());
        }));

=======
    public void tankSay(String data) {
        logger.info("tankSay==>>{}",data);
        tankServiceTwo.tankServiceTwo(data);
        tankServiceThere.tankServiceThere(data);
>>>>>>> f5dd40435e65e96099cc87880c6db7106c53e41f
    }
}
