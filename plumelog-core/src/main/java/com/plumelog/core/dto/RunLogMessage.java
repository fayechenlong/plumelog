package com.plumelog.core.dto;

/**
 * className：RunLogMessage
 * description：
 * time：2020-05-11.16:17
 *
 * @author Tank
 * @version 1.0.0
 */
public class RunLogMessage extends BaseLogMessage{


    private Long dtTime;
    private String content;
    private String logLevel;
    private String className;
    private String logType;
    private String dateTime;

    public Long getDtTime() {
        return dtTime;
    }

    public void setDtTime(Long dtTime) {
        this.dtTime = dtTime;
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

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"dtTime\":")
                .append(dtTime);
        sb.append(",\"content\":\"")
                .append(content).append('\"');
        sb.append(",\"logLevel\":\"")
                .append(logLevel).append('\"');
        sb.append(",\"className\":\"")
                .append(className).append('\"');
        sb.append(",\"logType\":\"")
                .append(logType).append('\"');
        sb.append(",\"dateTime\":\"")
                .append(dateTime).append('\"');
        sb.append(",\"method\":\"")
                .append(this.getMethod()).append('\"');
        sb.append(",\"appName\":\"")
                .append(this.getAppName()).append("\"");
        sb.append(",\"traceId\":\"")
                .append(this.getTraceId()).append("\"");
        sb.append(",\"serverName\":\"")
                .append(this.getServerName()).append("\"");
        sb.append('}');
        return sb.toString();
    }
}
