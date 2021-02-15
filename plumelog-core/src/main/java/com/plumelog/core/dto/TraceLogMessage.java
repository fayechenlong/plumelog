package com.plumelog.core.dto;

/**
 * className：TraceLogMessage
 * description：
 * time：2020-05-11.15:28
 *
 * @author Tank
 * @version 1.0.0
 */
public class TraceLogMessage extends BaseLogMessage {

ß
    /**
     * 执行的毫秒时间
     */
    private Long time;

    /**
     * 执行的位置 开始 or 结束
     */
    private String position;

    /**
     * 执行位置数
     */
    private Integer positionNum;

    public Integer getPositionNum() {
        return positionNum;
    }

    public void setPositionNum(Integer positionNum) {
        this.positionNum = positionNum;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"time\":")
                .append(time);
        sb.append(",\"position\":\"")
                .append(position).append('\"');
        sb.append(",\"positionNum\":")
                .append(positionNum);
        sb.append(",\"method\":")
                .append(this.getMethod());
        sb.append(",\"appName\":")
                .append(this.getAppName());
        sb.append(",\"traceId\":")
                .append(this.getTraceId());
        sb.append(",\"serverName\":")
                .append(this.getServerName());
        sb.append('}');
        return sb.toString();
    }
}
