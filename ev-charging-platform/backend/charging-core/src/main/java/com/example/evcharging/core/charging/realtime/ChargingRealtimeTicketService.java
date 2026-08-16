package com.example.evcharging.core.charging.realtime;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ChargingRealtimeTicketService {
    private static final Duration TTL = Duration.ofSeconds(60);
    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;

    public ChargingRealtimeTicketService(JdbcTemplate jdbc, StringRedisTemplate redis) { this.jdbc=jdbc; this.redis=redis; }

    public Ticket issue(String sessionNo) {
        long tenantId=RequestContext.requireTenantId();
        long userId=RequestContext.requireUserId();
        Integer count=jdbc.queryForObject(
                "SELECT COUNT(*) FROM charging_session WHERE tenant_id=? AND user_id=? AND session_no=?",
                Integer.class,tenantId,userId,sessionNo);
        if (count == null || count == 0) throw new IllegalArgumentException("session not found");
        String ticket = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(key(ticket), tenantId + "|" + sessionNo, TTL);
        return new Ticket(ticket, TTL.toSeconds());
    }

    public TicketIdentity consume(String ticket) {
        if (ticket == null || ticket.isBlank()) return null;
        String value = redis.opsForValue().getAndDelete(key(ticket));
        if (value == null) return null;
        String[] parts = value.split("\\|", 2);
        if (parts.length != 2) return null;
        try { return new TicketIdentity(Long.parseLong(parts[0]), parts[1]); } catch (NumberFormatException e) { return null; }
    }

    private static String key(String ticket) { return "ev:charging:ws-ticket:" + ticket; }
    public record Ticket(String ticket,long expiresInSeconds) {}
    public record TicketIdentity(long tenantId,String sessionNo) {}
}
