package com.example.evcharging.open.admin;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class OpenObservabilityService {
    private final JdbcTemplate jdbc;
    public OpenObservabilityService(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public List<AuditView> audits(int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        return jdbc.query("""
            SELECT id,partner_id,app_key,request_id,method,request_path,response_status,latency_ms,remote_ip,create_time
            FROM open_api_audit_log WHERE tenant_id=? ORDER BY id DESC LIMIT ?
            """,(rs,n)->new AuditView(rs.getLong(1),(Long)rs.getObject(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getInt(7),rs.getLong(8),rs.getString(9),String.valueOf(rs.getObject(10))),
                tenant,size);
    }

    public List<CallbackTaskView> callbacks(int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        return jdbc.query("""
            SELECT t.id,p.partner_code,t.callback_type,t.business_key,t.status,t.retry_count,t.response_status,
                   t.last_error,t.sent_time,t.create_time
            FROM open_partner_callback_task t JOIN open_partner_app p ON p.id=t.partner_id
            WHERE t.tenant_id=? ORDER BY t.id DESC LIMIT ?
            """,(rs,n)->new CallbackTaskView(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getInt(6),(Integer)rs.getObject(7),rs.getString(8),String.valueOf(rs.getObject(9)),
                String.valueOf(rs.getObject(10))),tenant,size);
    }

    @Transactional
    public void retryCallback(long id){
        long tenant=RequestContext.requireTenantId();LocalDateTime now=utcNow();
        int n=jdbc.update("""
            UPDATE open_partner_callback_task
            SET status='RETRY',next_retry_time=?,claim_token=NULL,claim_time=NULL,last_error=NULL,update_time=?
            WHERE tenant_id=? AND id=? AND status='DEAD'
            """,now,now,tenant,id);
        if(n!=1)throw new IllegalStateException("only DEAD callback task can be manually retried");
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
    public record AuditView(long id,Long partnerId,String appKey,String requestId,String method,String path,
                            int responseStatus,long latencyMs,String remoteIp,String createTime){}
    public record CallbackTaskView(long id,String partnerCode,String callbackType,String businessKey,String status,
                                   int retryCount,Integer responseStatus,String lastError,String sentTime,String createTime){}
}
