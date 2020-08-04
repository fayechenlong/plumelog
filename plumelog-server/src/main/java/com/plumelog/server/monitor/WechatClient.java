package com.plumelog.server.monitor;

import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.server.util.HttpClient;
import org.slf4j.LoggerFactory;

import java.util.*;

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

        if (plumeLogMonitorTextMessage.isAtAll() || plumeLogMonitorTextMessage.getAtMobiles() != null) {
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
