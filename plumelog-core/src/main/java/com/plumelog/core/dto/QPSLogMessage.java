package com.plumelog.core.dto;

/**
 * QPS统计日志
 *
 * @Author caijian
 * @Date 2020/12/17 7:53 pm
 */
public class QPSLogMessage {

    /**
     * 消息id
     * 解决消息被重复发送问题
     * 最后一次请求的时间戳
     */
    private String messageId;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * 应用名
     */
    private String appName;

    /**
     * 接口名
     */
    private String requestURI;

    /**
     * 本机ip
     */
    private String serverName;

    /**
     * 时间节点 yyyyMMddHHmmss
     */
    private Long dtTime;

    /**
     * 请求次数
     */
    private long incr;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Long getDtTime() {
        return dtTime;
    }

    public void setDtTime(Long dtTime) {
        this.dtTime = dtTime;
    }

    public long getIncr() {
        return incr;
    }

    public void setIncr(long incr) {
        this.incr = incr;
    }
}
