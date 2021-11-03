package com.plumelog.lite.client;

import javax.websocket.Session;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketSession{
        public static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();
}
