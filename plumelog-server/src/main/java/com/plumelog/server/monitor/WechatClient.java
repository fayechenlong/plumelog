package com.plumelog.server.monitor;

import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.server.util.HttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : pdl
 * @date : 2020/8/4 17:43
 */
public class WechatClient {

    public static void sendToWeChat(PlumeLogMonitorTextMessage plumeLogMonitorTextMessage, String URL) {
        Map<String, Object> requestBody = new HashMap<>(2);
        Map<String, Object> content = new HashMap<>(2);
        content.put("content", plumeLogMonitorTextMessage.getText());
        requestBody.put("msgtype", "markdown");
        requestBody.put("markdown", content);
        HttpClient.doPost(URL, GfJsonUtil.toJSONString(requestBody));

        boolean atMobiles = plumeLogMonitorTextMessage.getAtMobiles() != null && plumeLogMonitorTextMessage.getAtMobiles().size() > 0;
        if (plumeLogMonitorTextMessage.isAtAll() || atMobiles) {
            requestBody.clear();
            content.clear();
            List<String> mobiles = plumeLogMonitorTextMessage.getAtMobiles();
            List<String> mobileList = new ArrayList<>();
            if (mobiles.size() > 0) {
                mobileList.addAll(mobiles);
            }
            if (plumeLogMonitorTextMessage.isAtAll()) {
                mobileList.add("@all");
            }
            content.put("mentioned_mobile_list", mobileList);
            content.put("content", "");
            requestBody.put("msgtype", "text");
            requestBody.put("text", content);
            HttpClient.doPost(URL, GfJsonUtil.toJSONString(requestBody));
        }

    }
}
