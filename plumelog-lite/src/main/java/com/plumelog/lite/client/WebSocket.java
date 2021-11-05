package com.plumelog.lite.client;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/plumelog/websocket")
@Component
public class WebSocket {
    @OnClose
    public void onClose(Session session) {
        WebSocketSession.sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String params, Session session){
    }

    @OnError
    public  void onError(Session session, Throwable error){
        WebSocketSession.sessions.remove(session);
    }
}
