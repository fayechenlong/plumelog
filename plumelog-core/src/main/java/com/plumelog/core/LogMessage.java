package com.plumelog.core;

/**
 * className：LogMessage
 * description：LogMessage
 *             appName 应用名称用来区分日志属于哪个应用
 *             env 应用环境用来区分日志属于哪个应用环境
 *             serverName 应用运行所属IP地址
 *             traceId 应用traceId，配置了拦截器才能自动生成
 *             logType 日志类型，区分运行日志还是链路日志
 * @author Frank.chen
 * @version 1.0.0
 */
public class LogMessage {
    private String appName;
    private String env;
    private String serverName;
    private Long dtTime;
    private String traceId;
    private String content;
    private String logLevel;
    private String className;
    private String method;
    private String logType;
    private String dateTime;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getEnv() {
        return env;
    }
    
    public void setEnv(String env) {
        this.env = env;
    }
    
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public Long getDtTime() {
        return dtTime;
    }

    public void setDtTime(Long dtTime) {
        this.dtTime = dtTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
