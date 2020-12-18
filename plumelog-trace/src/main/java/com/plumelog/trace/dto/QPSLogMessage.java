package com.plumelog.trace.dto;

/**
 * QPS统计日志
 *
 * @Author caijian
 * @Date 2020/12/17 7:53 pm
 */
public class QPSLogMessage {

    /**
     * 应用名
     */
    private String appName;

    /**
     * 接口名
     */
    private String requestURL;

    /**
     * 本机ip
     */
    private String serverName;

    /**
     * 时间节点 yyyyMMddHHmmss
     */
    private String dtTime;

    /**
     * 请求次数
     */
    private long incr;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getDtTime() {
        return dtTime;
    }

    public void setDtTime(String dtTime) {
        this.dtTime = dtTime;
    }

    public long getIncr() {
        return incr;
    }

    public void setIncr(long incr) {
        this.incr = incr;
    }
}
