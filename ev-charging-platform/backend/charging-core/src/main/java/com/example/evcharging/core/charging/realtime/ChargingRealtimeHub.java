package com.example.evcharging.core.charging.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChargingRealtimeHub {
    private final ObjectMapper mapper;
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public ChargingRealtimeHub(ObjectMapper mapper) { this.mapper = mapper; }

    public void register(String sessionNo, WebSocketSession session) {
        sessions.computeIfAbsent(sessionNo, ignored -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void unregister(String sessionNo, WebSocketSession session) {
        sessions.computeIfPresent(sessionNo, (key, set) -> {
            set.remove(session);
            return set.isEmpty() ? null : set;
        });
    }

    public void publish(String sessionNo, Object payload) {
        Set<WebSocketSession> set = sessions.get(sessionNo);
        if (set == null || set.isEmpty()) return;
        try {
            TextMessage message = new TextMessage(mapper.writeValueAsString(payload));
            for (WebSocketSession session : set) {
                if (!session.isOpen()) continue;
                synchronized (session) { session.sendMessage(message); }
            }
        } catch (Exception e) {
            throw new IllegalStateException("cannot publish websocket charging update", e);
        }
    }
}
