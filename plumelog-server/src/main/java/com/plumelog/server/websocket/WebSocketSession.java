package com.plumelog.server.websocket;

import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.StringUtils;
import com.plumelog.server.util.GfJsonUtil;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketSession {
    public static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();
    public static ConcurrentHashMap<Session, String> sessionAppName = new ConcurrentHashMap<>();

    public static void sendToConsole(String message) {
        try {
            if (sessions.size() == 0) {
                return;
            }
            for (Session session : sessions) {
                String appName = sessionAppName.get(session);
                if (appName != null && !"".equals(appName)) {
                    RunLogMessage runLogMessage = GfJsonUtil.parseObject(message, RunLogMessage.class);
                    if (appName.equals(runLogMessage.getAppName())) {
                        session.getBasicRemote().sendText(message);
                    }
                } else {
                    session.getBasicRemote().sendText(message);
                }
            }
        } catch (Exception e) {

        }
    }
    public static void sendToConsole(RunLogMessage runLogMessage) {
        try {
            if (sessions.size() == 0) {
                return;
            }
            for (Session session : sessions) {
                String appName = sessionAppName.get(session);
                if (appName != null && !"".equals(appName)) {
                    if (appName.equals(runLogMessage.getAppName())) {
                        session.getBasicRemote().sendText(GfJsonUtil.toJSONString(runLogMessage));
                    }else if("all".equalsIgnoreCase(appName)) {
                        session.getBasicRemote().sendText(GfJsonUtil.toJSONString(runLogMessage));
                    }
                } else {
                    session.getBasicRemote().sendText(GfJsonUtil.toJSONString(runLogMessage));
                }
            }
        } catch (Exception e) {

        }
    }
}
