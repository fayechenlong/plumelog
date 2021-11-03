package com.plumelog.server.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/plumelog/websocket")
@Component
public class WebSocket {
    @OnOpen
    public void onOpen(Session session) {
        WebSocketSession.sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        WebSocketSession.sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String params, Session session) throws Exception {
        WebSocketSession.sessionAppName.put(session,params);
    }
}
