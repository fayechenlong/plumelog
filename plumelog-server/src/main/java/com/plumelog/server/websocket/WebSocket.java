package com.plumelog.server.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
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
        WebSocketSession.sessionAppName.remove(session);
    }

    @OnMessage
    public void onMessage(String params, Session session){
        WebSocketSession.sessionAppName.put(session,params);
    }

    @OnError
    public  void onError(Session session, Throwable error){
        WebSocketSession.sessions.remove(session);
        WebSocketSession.sessionAppName.remove(session);
    }
}
