package com.plumelog.lite.client;

import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.StringUtils;
import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketSession {
    public static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();
    public static ConcurrentHashMap<Session, Filter> sessionAppName = new ConcurrentHashMap<>();

    public static void sendToConsole(String message) {
        try {
            if (sessions.size() == 0) {
                return;
            }
            for (Session session : sessions) {
                RunLogMessage runLogMessage = GfJsonUtil.parseObject(message, RunLogMessage.class);
                send(session, runLogMessage, message);
            }
        } catch (Exception e) {

        }
    }

    public static void sendToConsole(RunLogMessage runLogMessage) {
        try {
            if (sessions.size() == 0) {
                return;
            }
            String message = GfJsonUtil.toJSONString(runLogMessage);
            for (Session session : sessions) {
                send(session, runLogMessage, message);
            }
        } catch (Exception e) {

        }
    }

    private static void send(Session session, RunLogMessage runLogMessage, String message) throws Exception {
        Filter filter = sessionAppName.get(session);
        if (filter != null && filter.getAppName() != null) {
            String appName = filter.getAppName();
            String env = filter.getEnv();
            String serverName = filter.getServerName();
            String level = filter.getLevel();

            if (StringUtils.isNotEmpty(appName)) {
                if (StringUtils.isEmpty(env) && StringUtils.isEmpty(serverName) && StringUtils.isEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName)) {
                        session.getBasicRemote().sendText(message);
                    }
                } else if (StringUtils.isNotEmpty(env) && StringUtils.isEmpty(serverName) && StringUtils.isEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName) && runLogMessage.getEnv().equals(env)) {
                        session.getBasicRemote().sendText(message);
                    }
                } else if (StringUtils.isNotEmpty(env) && StringUtils.isNotEmpty(serverName) && StringUtils.isEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName) && runLogMessage.getEnv().equals(env) && runLogMessage.getServerName().equals(serverName)) {
                        session.getBasicRemote().sendText(message);
                    }
                } else if (StringUtils.isEmpty(env) && StringUtils.isNotEmpty(serverName) && StringUtils.isEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName) && runLogMessage.getServerName().equals(serverName)) {
                        session.getBasicRemote().sendText(message);
                    }
                } else if (StringUtils.isNotEmpty(env) && StringUtils.isNotEmpty(serverName) && StringUtils.isNotEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName) && runLogMessage.getEnv().equals(env) && runLogMessage.getServerName().equals(serverName) && runLogMessage.getLogLevel().equals(level)) {
                        session.getBasicRemote().sendText(message);
                    }
                } else if (StringUtils.isNotEmpty(env) && StringUtils.isEmpty(serverName) && StringUtils.isNotEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName) && runLogMessage.getEnv().equals(env) && runLogMessage.getLogLevel().equals(level)) {
                        session.getBasicRemote().sendText(message);
                    }
                } else if (StringUtils.isEmpty(env) && StringUtils.isNotEmpty(serverName) && StringUtils.isNotEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName) && runLogMessage.getServerName().equals(serverName) && runLogMessage.getLogLevel().equals(level)) {
                        session.getBasicRemote().sendText(message);
                    }
                } else if (StringUtils.isEmpty(env) && StringUtils.isEmpty(serverName) && StringUtils.isNotEmpty(level)) {
                    if (runLogMessage.getAppName().equals(appName) && runLogMessage.getLogLevel().equals(level)) {
                        session.getBasicRemote().sendText(message);
                    }
                }
            }
        }
    }
}
