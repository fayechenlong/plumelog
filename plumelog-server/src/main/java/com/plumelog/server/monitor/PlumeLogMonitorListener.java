package com.plumelog.server.monitor;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * className：PlumeLogMonitorListener
 * description： 日志监控报警
 * time：2020-07-02.11:19
 *
 * @author Tank
 * @version 1.0.0
 */
@Component
public class PlumeLogMonitorListener implements ApplicationListener<PlumelogMonitorEvent> {


    @Async
    @Override
    public void onApplicationEvent(PlumelogMonitorEvent event) {
        System.out.println("getEvent" + event.getType() + "====" + Thread.currentThread().getName());
        System.out.println(event.getLogs());

    }
}
