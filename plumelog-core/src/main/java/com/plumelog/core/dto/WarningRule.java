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
    private String env;
    private String appCategory;
    private String className;
    private String receiver;
    private String webhookUrl;
    private int errorCount;
    private int time;
    private int status;
    private int hookServe = 1; // 1 DingTalk 2 Wechat

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
    
    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getHookServe() {
        return hookServe;
    }

    public void setHookServe(int hookServe) {
        this.hookServe = hookServe;
    }
}
