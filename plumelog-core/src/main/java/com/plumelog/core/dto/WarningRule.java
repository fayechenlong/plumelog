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

}
