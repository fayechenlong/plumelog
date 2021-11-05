package com.plumelog.lite.client;

import com.plumelog.core.util.GfJsonUtil;
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
        Filter filter= GfJsonUtil.parseObject(params,Filter.class);
        WebSocketSession.sessionAppName.put(session,filter);
    }

    @OnError
    public  void onError(Session session, Throwable error){
        WebSocketSession.sessions.remove(session);
        WebSocketSession.sessionAppName.remove(session);
    }
}
