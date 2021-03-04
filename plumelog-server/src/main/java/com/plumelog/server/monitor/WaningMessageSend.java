package com.plumelog.server.monitor;
import com.plumelog.core.dto.WarningRule;

/**
 * @author chenlongfei
 */
public class WaningMessageSend {

    public static void send(WarningRule rule,PlumeLogMonitorTextMessage message){

        if (rule.getHookServe() == 1) {
            DingTalkClient.sendToDingTalk(message, rule.getWebhookUrl());
        } else {
            WechatClient.sendToWeChat(message, rule.getWebhookUrl());
        }
    }
}
