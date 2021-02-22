package com.plumelog.core.dto;

import com.plumelog.core.util.GfJsonUtil;

/**
 * className：TraceLogMessage
 * description：
 * time：2020-05-11.15:28
 *
 * @author Tank
 * @version 1.0.0
 */
public class TraceLogMessage extends BaseLogMessage {


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
                .append(positionNum).append("");
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

    public static void main(String[] args) {
        String json = "{\"appName\":\"5GOperationPlatformBE\",\"method\":\"void com.ai.op.xcore.be.security.service.UserCacheCleanService.cleanUserCache(String)\",\"position\":\">\",\"positionNum\":36,\"serverName\":\"10.21.10.39\",\"time\":1613413364793,\"traceId\":\"116472029800173568\"}";
        TraceLogMessage t = GfJsonUtil.parseObject(json,TraceLogMessage.class);
        System.out.println(GfJsonUtil.parseObject(t.toString(),TraceLogMessage.class));
        System.out.println(GfJsonUtil.toJSONString(t));
    }
}
