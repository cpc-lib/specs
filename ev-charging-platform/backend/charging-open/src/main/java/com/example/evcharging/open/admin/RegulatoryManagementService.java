package com.example.evcharging.open.admin;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.open.regulatory.GbT44130CanonicalAdapter;
import com.example.evcharging.open.security.SecretCipher;
import com.example.evcharging.open.security.OutboundUrlPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class RegulatoryManagementService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;private final SecretCipher cipher;private final OutboundUrlPolicy outbound;
    public RegulatoryManagementService(JdbcTemplate jdbc,IdGenerator ids,SecretCipher cipher,OutboundUrlPolicy outbound){
        this.jdbc=jdbc;this.ids=ids;this.cipher=cipher;this.outbound=outbound;
    }

    @Transactional
    public long create(CreatePlatformCommand c){
        long tenant=RequestContext.requireTenantId();
        outbound.requireAllowed(c.endpointUrl());
        String protocol=c.protocolCode()==null||c.protocolCode().isBlank()?GbT44130CanonicalAdapter.PROTOCOL:c.protocolCode();
        long id=ids.nextId();LocalDateTime now=utcNow();
        jdbc.update("""
            INSERT INTO open_regulatory_platform(
              id,tenant_id,platform_code,platform_name,protocol_code,endpoint_url,credential_key,
              credential_secret_ciphertext,enabled,public_info_enabled,business_info_enabled,
              rate_limit_per_minute,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """,id,tenant,c.platformCode(),c.platformName(),protocol,c.endpointUrl(),c.credentialKey(),
                cipher.encrypt(c.credentialSecret()),true,c.publicInfoEnabled(),c.businessInfoEnabled(),
                c.rateLimitPerMinute()<=0?120:c.rateLimitPerMinute(),now,now);
        return id;
    }

    @Transactional
    public void update(long id,UpdatePlatformCommand c){
        long tenant=RequestContext.requireTenantId();
        outbound.requireAllowed(c.endpointUrl());
        int n=jdbc.update("""
            UPDATE open_regulatory_platform SET endpoint_url=?,enabled=?,public_info_enabled=?,
              business_info_enabled=?,rate_limit_per_minute=?,update_time=?
            WHERE tenant_id=? AND id=?
            """,c.endpointUrl(),c.enabled(),c.publicInfoEnabled(),c.businessInfoEnabled(),
                c.rateLimitPerMinute(),utcNow(),tenant,id);
        if(n!=1)throw new IllegalArgumentException("regulatory platform not found");
    }

    public List<PlatformView> list(){
        long tenant=RequestContext.requireTenantId();
        return jdbc.query("""
            SELECT id,platform_code,platform_name,protocol_code,endpoint_url,enabled,
                   public_info_enabled,business_info_enabled,rate_limit_per_minute,create_time
            FROM open_regulatory_platform WHERE tenant_id=? ORDER BY id DESC
            """,(rs,n)->new PlatformView(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getBoolean(6),rs.getBoolean(7),rs.getBoolean(8),rs.getInt(9),String.valueOf(rs.getObject(10))),tenant);
    }

    public List<TaskView> tasks(int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        return jdbc.query("""
            SELECT t.id,p.platform_code,t.data_type,t.business_key,t.status,t.retry_count,t.response_status,
                   t.last_error,t.reported_time,t.create_time
            FROM open_regulatory_report_task t JOIN open_regulatory_platform p ON p.id=t.platform_id
            WHERE t.tenant_id=? ORDER BY t.id DESC LIMIT ?
            """,(rs,n)->new TaskView(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getInt(6),(Integer)rs.getObject(7),rs.getString(8),String.valueOf(rs.getObject(9)),
                String.valueOf(rs.getObject(10))),tenant,size);
    }

    @Transactional
    public void retry(long taskId){
        long tenant=RequestContext.requireTenantId();
        outbound.requireAllowed(c.endpointUrl());
        int n=jdbc.update("""
            UPDATE open_regulatory_report_task
            SET status='RETRY',next_retry_time=?,claim_token=NULL,claim_time=NULL,last_error=NULL,update_time=?
            WHERE tenant_id=? AND id=? AND status='DEAD'
            """,utcNow(),utcNow(),tenant,taskId);
        if(n!=1)throw new IllegalStateException("only DEAD regulatory task can be manually retried");
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
    public record CreatePlatformCommand(String platformCode,String platformName,String protocolCode,String endpointUrl,
                                        String credentialKey,String credentialSecret,boolean publicInfoEnabled,
                                        boolean businessInfoEnabled,int rateLimitPerMinute){}
    public record UpdatePlatformCommand(String endpointUrl,boolean enabled,boolean publicInfoEnabled,
                                        boolean businessInfoEnabled,int rateLimitPerMinute){}
    public record PlatformView(long id,String platformCode,String platformName,String protocolCode,String endpointUrl,
                               boolean enabled,boolean publicInfoEnabled,boolean businessInfoEnabled,
                               int rateLimitPerMinute,String createTime){}
    public record TaskView(long id,String platformCode,String dataType,String businessKey,String status,int retryCount,
                           Integer responseStatus,String lastError,String reportedTime,String createTime){}
}
