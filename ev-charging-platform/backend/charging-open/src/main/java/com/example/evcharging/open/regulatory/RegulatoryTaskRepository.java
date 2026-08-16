package com.example.evcharging.open.regulatory;

import com.example.evcharging.open.security.SecretCipher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

@Repository
public class RegulatoryTaskRepository {
    private final JdbcTemplate jdbc;private final SecretCipher cipher;
    public RegulatoryTaskRepository(JdbcTemplate jdbc,SecretCipher cipher){this.jdbc=jdbc;this.cipher=cipher;}

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public Claimed claim(long id){
        String token=UUID.randomUUID().toString();LocalDateTime now=utcNow();
        int updated=jdbc.update("""
            UPDATE open_regulatory_report_task
            SET status='SENDING',claim_token=?,claim_time=?,update_time=?
            WHERE id=? AND status IN ('PENDING','RETRY') AND next_retry_time<=?
            """,token,now,now,id,now);
        if(updated!=1)return null;
        return jdbc.queryForObject("""
            SELECT t.id,t.data_type,t.business_key,t.source_payload_json,t.retry_count,
                   p.id,p.tenant_id,p.platform_code,p.protocol_code,p.endpoint_url,p.credential_key,
                   p.credential_secret_ciphertext,p.rate_limit_per_minute
            FROM open_regulatory_report_task t
            JOIN open_regulatory_platform p ON p.id=t.platform_id
            WHERE t.id=? AND t.claim_token=? AND p.enabled=1
            """,(rs,n)->new Claimed(
                rs.getLong(1),token,rs.getString(2),rs.getString(3),rs.getString(4),rs.getInt(5),
                rs.getLong(6),rs.getLong(7),rs.getString(8),rs.getString(9),rs.getString(10),rs.getString(11),
                rs.getString(12)==null?null:cipher.decrypt(rs.getString(12)),rs.getInt(13)),id,token);
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void success(Claimed task,int responseStatus,String body){
        jdbc.update("""
            UPDATE open_regulatory_report_task
            SET status='SENT',response_status=?,response_body=?,last_error=NULL,reported_time=?,
                claim_token=NULL,claim_time=NULL,update_time=?
            WHERE id=? AND status='SENDING' AND claim_token=?
            """,responseStatus,truncate(body,2000),utcNow(),utcNow(),task.id(),task.claimToken());
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void failure(Claimed task,String error,Integer responseStatus,String responseBody){
        int retry=task.retryCount()+1;String status=retry>=8?"DEAD":"RETRY";LocalDateTime now=utcNow();
        long delay=Math.min(3600L,(1L<<Math.min(retry,6))*20L);
        jdbc.update("""
            UPDATE open_regulatory_report_task
            SET status=?,retry_count=?,response_status=?,response_body=?,last_error=?,next_retry_time=?,
                claim_token=NULL,claim_time=NULL,update_time=?
            WHERE id=? AND status='SENDING' AND claim_token=?
            """,status,retry,responseStatus,truncate(responseBody,2000),truncate(error,1000),
                now.plusSeconds(delay),now,task.id(),task.claimToken());
    }

    private static String truncate(String v,int max){return v==null?null:v.length()<=max?v:v.substring(0,max);}
    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}

    public record Claimed(long id,String claimToken,String dataType,String businessKey,String sourcePayloadJson,int retryCount,
                          long platformId,long tenantId,String platformCode,String protocolCode,String endpointUrl,
                          String credentialKey,String credentialSecret,int rateLimitPerMinute){}
}
