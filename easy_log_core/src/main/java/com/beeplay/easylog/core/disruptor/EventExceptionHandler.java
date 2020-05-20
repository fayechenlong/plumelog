package com.beeplay.easylog.core.disruptor;

import com.lmax.disruptor.ExceptionHandler;

/**
 * className：EventExceptionHandler
 * description： TODO
 * time：2020-05-19.15:28
 *
 * @author Tank
 * @version 1.0.0
 */
public class EventExceptionHandler implements ExceptionHandler<LogEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, LogEvent event) {

    }

    @Override
    public void handleOnStartException(Throwable ex) {

    }

    @Override
    public void handleOnShutdownException(Throwable ex) {

    }
}
