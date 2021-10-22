package com.plumelog.server.monitor;

import com.plumelog.core.dto.WarningRule;
import com.plumelog.core.util.HttpClient;

/**
 * @author chenlongfei
 */
public class WaningMessageSend {

    public static void send(WarningRule rule, PlumeLogMonitorTextMessage message) {
        if (rule.getHookServe() == 1) {
            DingTalkClient.sendToDingTalk(message, rule.getWebhookUrl());
        } else if (rule.getHookServe() == 2) {
            WechatClient.sendToWeChat(message, rule.getWebhookUrl());
        } else if (rule.getHookServe() == 3) {
            FeishuClient.sendToFeishu(message, rule.getWebhookUrl());
        } else {
            String param = "message=" + message.getText();
            param = param + "&mobile=" + message.getAtMobiles();
            HttpClient.doPost(rule.getWebhookUrl(), param);
        }
    }
}
