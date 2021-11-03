package com.plumelog.lite.client;

import org.springframework.stereotype.Component;

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
    public void onClose(Session session) {
        WebSocketSession.sessions.remove(session);
    }
}
