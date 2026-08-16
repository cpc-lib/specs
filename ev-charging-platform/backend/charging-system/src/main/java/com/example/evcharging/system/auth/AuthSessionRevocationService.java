package com.example.evcharging.system.auth;

import com.example.evcharging.framework.webmvc.TokenRevocationChecker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthSessionRevocationService {
    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;

    public AuthSessionRevocationService(JdbcTemplate jdbc,StringRedisTemplate redis){
        this.jdbc=jdbc;this.redis=redis;
    }

    @Transactional
    public void revokeSession(long tenantId,String sessionId,String reason){
        List<Session> rows=jdbc.query("""
            SELECT expires_time,status FROM sys_auth_session
            WHERE tenant_id=? AND session_id=? FOR UPDATE
            """,(rs,n)->new Session(rs.getObject(1,LocalDateTime.class),rs.getString(2)),tenantId,sessionId);
        if(rows.isEmpty()) return;
        Session s=rows.get(0);
        if(!"REVOKED".equals(s.status())){
            jdbc.update("""
                UPDATE sys_auth_session SET status='REVOKED',revoked_time=?,revoke_reason=?
                WHERE tenant_id=? AND session_id=? AND status<>'REVOKED'
                """,utcNow(),reason,tenantId,sessionId);
        }
        markRedis(sessionId,s.expiresTime());
    }

    @Transactional
    public int revokeUserSessions(long tenantId,long userId,String reason){
        List<SessionId> sessions=jdbc.query("""
            SELECT session_id,expires_time FROM sys_auth_session
            WHERE tenant_id=? AND user_id=? AND status='ACTIVE'
            FOR UPDATE
            """,(rs,n)->new SessionId(rs.getString(1),rs.getObject(2,LocalDateTime.class)),tenantId,userId);
        int updated=jdbc.update("""
            UPDATE sys_auth_session SET status='REVOKED',revoked_time=?,revoke_reason=?
            WHERE tenant_id=? AND user_id=? AND status='ACTIVE'
            """,utcNow(),reason,tenantId,userId);
        for(SessionId s:sessions) markRedis(s.sessionId(),s.expiresTime());
        return updated;
    }

    private void markRedis(String sessionId,LocalDateTime expires){
        long seconds=Math.max(60,Duration.between(utcNow(),expires).toSeconds());
        redis.opsForValue().set(TokenRevocationChecker.sessionKey(sessionId),"1",seconds,TimeUnit.SECONDS);
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}

    private record Session(LocalDateTime expiresTime,String status){}
    private record SessionId(String sessionId,LocalDateTime expiresTime){}
}
