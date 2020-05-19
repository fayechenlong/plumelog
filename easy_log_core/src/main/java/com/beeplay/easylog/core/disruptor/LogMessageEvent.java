package com.beeplay.easylog.core.disruptor;

import com.beeplay.easylog.core.LogMessage;

/**
 * @ClassName LongEvent
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 20:11
 * @Version 1.0
 **/
public class LogMessageEvent {
    private String value;

    public void set(String value) {
        this.value = value;
    }

    public String get(){
        return value;
    }
}
