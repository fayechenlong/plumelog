package com.plumelog.server.monitor;

import com.plumelog.server.websocket.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

/**
 * 利用redis的发布订阅，推送控制台消息
 *
 * @author frank.chen
 * @date 2021年11月9日
 * @version 1.0
 */

public class RedisMsgPubSubListener extends JedisPubSub {
    private static final Logger logger = LoggerFactory.getLogger(RedisMsgPubSubListener.class);
    @Override
    public void onMessage(String channel, String message) {
        WebSocketSession.sendToConsole(message);
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        logger.info("开始订阅滚动日志！");
    }
}


