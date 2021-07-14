package com.plumelog.server.monitor;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.slf4j.LoggerFactory;

public class DingTalkClient {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DingTalkClient.class);

    public static void sendToDingTalk(PlumeLogMonitorTextMessage plumeLogMonitorTextMessage, String URL) {
        com.dingtalk.api.DingTalkClient client = new DefaultDingTalkClient(URL);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("报警通知");
        markdown.setText(plumeLogMonitorTextMessage.getText());
        request.setMarkdown(markdown);
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setAtMobiles(plumeLogMonitorTextMessage.getAtMobiles());
        at.setIsAtAll(plumeLogMonitorTextMessage.isAtAll());
        request.setAt(at);
        OapiRobotSendResponse response = null;
        try {
            response = client.execute(request);
        } catch (ApiException e) {
            e.printStackTrace();
            logger.error(response.getErrmsg());
        }
    }
}
