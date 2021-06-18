package com.plumelog.core.dto;

/**
 * className：BaseLogMessage
 * description：
 * time：2020-05-11.15:28
 *
 * @author Tank
 * @version 1.0.0
 */
public class BaseLogMessage {
    /**
     * 记录服务IP
     */
    private String serverName;

    /**
     * 追踪码
     */
    private String traceId;

    /**
     * 应用名
     */
    private String appName;
    
    /**
     * 应用环境
     */
    private String env;
    
    /**
     * 方法名
     */
    private String method;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public void setEnv(String env) {
        this.env = env;
    }
    
    public String getEnv() {
        return env;
    }
    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public String getAppNameWithEnv() {
        return this.appName + "-_-" + this.env;
    }
}
