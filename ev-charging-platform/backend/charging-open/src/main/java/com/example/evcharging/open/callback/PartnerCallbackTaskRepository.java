package com.example.evcharging.open.callback;

import com.example.evcharging.open.security.SecretCipher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

@Repository
public class PartnerCallbackTaskRepository {
    private final JdbcTemplate jdbc;private final SecretCipher cipher;
    public PartnerCallbackTaskRepository(JdbcTemplate jdbc,SecretCipher cipher){this.jdbc=jdbc;this.cipher=cipher;}

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public Claimed claim(long id){
        String token=UUID.randomUUID().toString();LocalDateTime now=utcNow();
        int updated=jdbc.update("""
            UPDATE open_partner_callback_task
            SET status='SENDING',claim_token=?,claim_time=?,update_time=?
            WHERE id=? AND status IN ('PENDING','RETRY') AND next_retry_time<=?
            """,token,now,now,id,now);
        if(updated!=1)return null;
        return jdbc.queryForObject("""
            SELECT t.id,t.payload_json,t.retry_count,p.callback_url,p.callback_secret_ciphertext
            FROM open_partner_callback_task t JOIN open_partner_app p ON p.id=t.partner_id
            WHERE t.id=? AND t.claim_token=?
            """,(rs,n)->new Claimed(rs.getLong(1),token,rs.getString(2),rs.getInt(3),rs.getString(4),
                cipher.decrypt(rs.getString(5))),id,token);
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void success(Claimed task,int status,String response){
        jdbc.update("""
            UPDATE open_partner_callback_task
            SET status='SENT',response_status=?,response_body=?,last_error=NULL,sent_time=?,claim_token=NULL,
                claim_time=NULL,update_time=?
            WHERE id=? AND status='SENDING' AND claim_token=?
            """,status,truncate(response,2000),utcNow(),utcNow(),task.id(),task.claimToken());
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void failure(Claimed task,String error,Integer responseStatus,String responseBody){
        int retry=task.retryCount()+1;String status=retry>=8?"DEAD":"RETRY";LocalDateTime now=utcNow();
        long delay=Math.min(3600L,(1L<<Math.min(retry,6))*15L);
        jdbc.update("""
            UPDATE open_partner_callback_task
            SET status=?,retry_count=?,response_status=?,response_body=?,last_error=?,next_retry_time=?,
                claim_token=NULL,claim_time=NULL,update_time=?
            WHERE id=? AND status='SENDING' AND claim_token=?
            """,status,retry,responseStatus,truncate(responseBody,2000),truncate(error,1000),
                now.plusSeconds(delay),now,task.id(),task.claimToken());
    }

    private static String truncate(String v,int max){return v==null?null:v.length()<=max?v:v.substring(0,max);}
    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
    public record Claimed(long id,String claimToken,String payloadJson,int retryCount,String callbackUrl,String callbackSecret){}
}
