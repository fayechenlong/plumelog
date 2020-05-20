package com.beeplay.easylog.core.disruptor;

import com.beeplay.easylog.core.AbstractClient;
import com.beeplay.easylog.core.dto.BaseLogMessage;

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
     * AppName
     */
    private String appName;

    /**
     * 日志主体
     */

    private BaseLogMessage baseLogMessage;

    /**
     * 客户端
     */
    private AbstractClient client;



    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public BaseLogMessage getBaseLogMessage() {
        return baseLogMessage;
    }

    public void setBaseLogMessage(BaseLogMessage baseLogMessage) {
        this.baseLogMessage = baseLogMessage;
    }

    public AbstractClient getClient() {
        return client;
    }

    public void setClient(AbstractClient client) {
        this.client = client;
    }

    public LogEvent(String appName, BaseLogMessage baseLogMessage, AbstractClient client) {
        this.appName = appName;
        this.baseLogMessage = baseLogMessage;
        this.client = client;
    }

    public LogEvent() {

    }
}
