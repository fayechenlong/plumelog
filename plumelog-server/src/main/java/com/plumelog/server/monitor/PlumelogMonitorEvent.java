package com.plumelog.server.monitor;

import com.plumelog.core.dto.RunLogMessage;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * className：PlumelogMonitorEvent
 * description： 日志报警事件
 * time：2020-07-02.10:52
 *
 * @author Tank
 * @version 1.0.0
 */

public class PlumelogMonitorEvent extends ApplicationEvent {

    /**
     * 日志信息列表
     */
    List<RunLogMessage> logs;


    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public PlumelogMonitorEvent(Object source, List<RunLogMessage> logs) {
        super(source);
        this.logs = logs;
    }

    public List<RunLogMessage> getLogs() {
        return logs;
    }

}
