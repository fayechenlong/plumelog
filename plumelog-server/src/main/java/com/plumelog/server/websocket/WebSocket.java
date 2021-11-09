package com.plumelog.server.websocket;

import com.plumelog.core.client.AbstractClient;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.IpGetter;
import com.plumelog.server.InitConfig;
import com.plumelog.server.util.GfJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;

@ServerEndpoint(value = "/plumelog/websocket")
@Component
public class WebSocket {
    private static final Logger logger = LoggerFactory.getLogger(WebSocket.class);
    private static AbstractClient redisClient;


    @Autowired
    public void setRedisClient(AbstractClient redisClient) {
        WebSocket.redisClient = redisClient;
    }

    @OnOpen
    public void onOpen(Session session) {
        WebSocketSession.sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        WebSocketSession.sessions.remove(session);
        WebSocketSession.sessionAppName.remove(session);

        if (!InitConfig.START_MODEL.equals(InitConfig.LITE_MODE_NAME)) {
            String sessionID = session.getId() + "/" + IpGetter.getIp();
            redisClient.hdel(InitConfig.WEB_CONSOLE_KEY, sessionID);
            logger.info("滚动日志关闭连接！id:{}", sessionID);
            Map<String, String> filters = redisClient.hgetAll(InitConfig.WEB_CONSOLE_KEY);
            for (String key : filters.keySet()) {
                logger.info("当前在线：");
                logger.info("连接：" + key + ";过滤条件：" + filters.get(key));
            }
        }

    }

    @OnMessage
    public void onMessage(String params, Session session) {
        Filter filter = GfJsonUtil.parseObject(params, Filter.class);
        WebSocketSession.sessionAppName.put(session, filter);
        if (!InitConfig.START_MODEL.equals(InitConfig.LITE_MODE_NAME)) {
            String sessionID = session.getId() + "/" + IpGetter.getIp();
            redisClient.hset(InitConfig.WEB_CONSOLE_KEY, sessionID, params);
            redisClient.expire(InitConfig.WEB_CONSOLE_KEY, 3600);
            logger.info("滚动日志连接！id:{} filter:{}", sessionID, params);
            Map<String, String> filters = redisClient.hgetAll(InitConfig.WEB_CONSOLE_KEY);
            for (String key : filters.keySet()) {
                try {
                    RunLogMessage runLogMessage = new RunLogMessage();
                    runLogMessage.setAppName("system");
                    runLogMessage.setDtTime(System.currentTimeMillis());
                    runLogMessage.setLogLevel("INFO");
                    runLogMessage.setContent("当前在线连接：" + key + ";过滤条件：" + filters.get(key));
                    runLogMessage.setServerName(IpGetter.getIp());
                    runLogMessage.setClassName("console");
                    runLogMessage.setMethod("out");
                    session.getBasicRemote().sendText(GfJsonUtil.toJSONString(runLogMessage));
                } catch (IOException e) {
                }
            }
        }

    }

    @OnError
    public void onError(Session session, Throwable error) {
        WebSocketSession.sessions.remove(session);
        WebSocketSession.sessionAppName.remove(session);
        if (!InitConfig.START_MODEL.equals(InitConfig.LITE_MODE_NAME)) {
            String sessionID = session.getId() + "/" + IpGetter.getLocalIP();
            redisClient.hdel(InitConfig.WEB_CONSOLE_KEY, sessionID);
            logger.error("滚动日志异常关闭连接！id:{}", sessionID, error);
            Map<String, String> filters = redisClient.hgetAll(InitConfig.WEB_CONSOLE_KEY);
            for (String key : filters.keySet()) {
                logger.info("当前在线：");
                logger.info("连接：" + key + ";过滤条件：" + filters.get(key));
            }
        }

    }
}
