package com.beeplay.easylog.core.disruptor;


import com.beeplay.easylog.core.util.GfJsonUtil;
import com.lmax.disruptor.EventHandler;

/**
 * @ClassName LongEventHandler
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 20:12
 * @Version 1.0
 **/
public class LogMessageEventHandler implements EventHandler<LogMessageEvent> {
    private String clientName;

    LogMessageEventHandler(String clientName){
        this.clientName = clientName;
    }

    //事件监听，类似观察者模式\
    @Override
    public void onEvent(LogMessageEvent event, long sequence, boolean endOfBatch) throws Exception {
       System.out.println(this.clientName + event.get());//向Client推送消息
    }
}
