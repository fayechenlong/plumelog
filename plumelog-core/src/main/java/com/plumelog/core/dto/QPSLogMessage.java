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

    /**
     * 最大处理时间
     */
    private long maxTime;

    /**
     * 最小处理时间
     */
    private long minTime;

    /**
     * 平均处理时间
     */
    private long avgTime;


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

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public long getMinTime() {
        return minTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public long getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(long avgTime) {
        this.avgTime = avgTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"messageId\":\"")
                .append(messageId).append('\"');
        sb.append(",\"namespace\":\"")
                .append(namespace).append('\"');
        sb.append(",\"appName\":\"")
                .append(appName).append('\"');
        sb.append(",\"requestURI\":\"")
                .append(requestURI).append('\"');
        sb.append(",\"serverName\":\"")
                .append(serverName).append('\"');
        sb.append(",\"dtTime\":")
                .append(dtTime);
        sb.append(",\"incr\":")
                .append(incr);
        sb.append(",\"maxTime\":")
                .append(maxTime);
        sb.append(",\"minTime\":")
                .append(minTime);
        sb.append(",\"avgTime\":")
                .append(avgTime);
        sb.append('}');
        return sb.toString();
    }
}
