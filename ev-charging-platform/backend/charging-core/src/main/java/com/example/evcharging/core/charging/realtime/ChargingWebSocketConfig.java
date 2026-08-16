package com.example.evcharging.core.charging.realtime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Configuration
@EnableWebSocket
public class ChargingWebSocketConfig implements WebSocketConfigurer {
    private final ChargingRealtimeHub hub;
    private final ChargingRealtimeTicketService tickets;
    private final String[] allowedOrigins;

    public ChargingWebSocketConfig(ChargingRealtimeHub hub, ChargingRealtimeTicketService tickets,
            @Value("${charging.websocket.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") String allowedOrigins) {
        this.hub = hub; this.tickets = tickets; this.allowedOrigins = allowedOrigins.split(",");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new Handler(hub), "/ws/charging")
                .addInterceptors(new TicketHandshakeInterceptor(tickets))
                .setAllowedOriginPatterns(allowedOrigins);
    }

    private static final class Handler extends TextWebSocketHandler {
        private final ChargingRealtimeHub hub;
        private Handler(ChargingRealtimeHub hub) { this.hub = hub; }
        @Override public void afterConnectionEstablished(WebSocketSession session) { hub.register((String) session.getAttributes().get("sessionNo"), session); }
        @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) { hub.unregister((String) session.getAttributes().get("sessionNo"), session); }
    }

    private static final class TicketHandshakeInterceptor implements HandshakeInterceptor {
        private final ChargingRealtimeTicketService tickets;
        private TicketHandshakeInterceptor(ChargingRealtimeTicketService tickets) { this.tickets = tickets; }
        @Override public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                org.springframework.http.server.ServerHttpResponse response, WebSocketHandler wsHandler, Map<String,Object> attributes) {
            String ticket = query(request.getURI()).get("ticket");
            ChargingRealtimeTicketService.TicketIdentity identity = tickets.consume(ticket);
            if (identity == null) return false;
            attributes.put("tenantId", identity.tenantId());
            attributes.put("sessionNo", identity.sessionNo());
            return true;
        }
        @Override public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                org.springframework.http.server.ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
        private static Map<String,String> query(URI uri) {
            java.util.HashMap<String,String> map = new java.util.HashMap<>(); String q = uri.getRawQuery(); if(q==null)return map;
            for(String pair:q.split("&")){String[] p=pair.split("=",2);if(p.length==2)map.put(p[0],java.net.URLDecoder.decode(p[1],java.nio.charset.StandardCharsets.UTF_8));}
            return map;
        }
    }
}
