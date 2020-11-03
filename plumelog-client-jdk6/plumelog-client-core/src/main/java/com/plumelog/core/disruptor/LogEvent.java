package com.plumelog.core.disruptor;

import com.plumelog.core.dto.BaseLogMessage;

import java.io.Serializable;

/**
 * className：LogEvent
 * description： TODO
 * time：2020-05-19.14:46
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogEvent implements Serializable {
    /**
     * 日志主体
     */

    private BaseLogMessage baseLogMessage;

    public LogEvent() {

    }

    public BaseLogMessage getBaseLogMessage() {
        return baseLogMessage;
    }

    public void setBaseLogMessage(BaseLogMessage baseLogMessage) {
        this.baseLogMessage = baseLogMessage;
    }
}
