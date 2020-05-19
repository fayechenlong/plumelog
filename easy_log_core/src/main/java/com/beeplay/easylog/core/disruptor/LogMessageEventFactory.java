package com.beeplay.easylog.core.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @ClassName LongEventFactory
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 20:11
 * @Version 1.0
 **/
public class LogMessageEventFactory implements EventFactory<LogMessageEvent> {

    @Override
    public LogMessageEvent newInstance() {
        return new LogMessageEvent();
    }
}
