package com.plumelog.core.dto;

/**
 * className：WarningRule
 * description：错误告警规则
 * time：2020/7/2  10:52
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class WarningRule {

    private String appName;
    private String className;
    private String receiver;
    private String webhookUrl;
    private int errorCount;
    private int time;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
