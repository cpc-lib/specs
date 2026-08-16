package com.example.evcharging.open.callback;

import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class PartnerCallbackService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;private final ObjectMapper mapper;
    public PartnerCallbackService(JdbcTemplate jdbc,IdGenerator ids,ObjectMapper mapper){this.jdbc=jdbc;this.ids=ids;this.mapper=mapper;}

    public void schedule(long tenantId,long partnerId,String type,String businessKey,Object payload){
        try{
            String json=mapper.writeValueAsString(payload);LocalDateTime now=utcNow();
            jdbc.update("""
                INSERT INTO open_partner_callback_task(
                  id,tenant_id,partner_id,callback_type,business_key,payload_json,status,retry_count,
                  next_retry_time,create_time,update_time
                ) VALUES (?,?,?,?,?,?,'PENDING',0,?,?,?)
                """,ids.nextId(),tenantId,partnerId,type,businessKey,json,now,now,now);
        }catch(DuplicateKeyException duplicate){
            // callback_type + business_key is idempotent for one partner
        }catch(Exception e){throw new IllegalStateException("cannot schedule partner callback",e);}
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
}
