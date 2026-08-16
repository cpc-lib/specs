package com.example.evcharging.open.regulatory;

import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.open.security.OpenApiSignature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.*;

@Service
public class RegulatoryReportService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;private final ObjectMapper mapper;
    public RegulatoryReportService(JdbcTemplate jdbc,IdGenerator ids,ObjectMapper mapper){this.jdbc=jdbc;this.ids=ids;this.mapper=mapper;}

    public void enqueue(long tenantId,long platformId,String dataType,String businessKey,Object payload){
        try{
            String json=mapper.writeValueAsString(payload);
            String hash=OpenApiSignature.sha256Hex(json.getBytes(StandardCharsets.UTF_8));
            LocalDateTime now=utcNow();
            jdbc.update("""
                INSERT INTO open_regulatory_report_task(
                  id,tenant_id,platform_id,data_type,business_key,source_payload_json,payload_hash,status,
                  retry_count,next_retry_time,create_time,update_time
                ) VALUES (?,?,?,?,?,?,?,'PENDING',0,?,?,?)
                """,ids.nextId(),tenantId,platformId,dataType,businessKey,json,hash,now,now,now);
        }catch(DuplicateKeyException duplicate){
            // same platform/data/business/payload is already scheduled or reported
        }catch(Exception e){throw new IllegalStateException("cannot enqueue regulatory report",e);}
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
}
