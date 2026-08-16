package com.example.evcharging.core.charging.recovery;

import com.example.evcharging.core.charging.domain.ChargingSessionStatus;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ChargingRecoveryWorker {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final ObjectMapper mapper;
    private final int maxAttempts;

    public ChargingRecoveryWorker(JdbcTemplate jdbc, IdGenerator ids, ObjectMapper mapper,
            @Value("${charging.recovery.max-attempts:3}") int maxAttempts) {
        this.jdbc=jdbc; this.ids=ids; this.mapper=mapper; this.maxAttempts=maxAttempts;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void recoverOne(long sessionId) throws Exception {
        Map<String,Object> s = jdbc.queryForMap("SELECT id,tenant_id,session_no,status,device_id,connector_no,recovery_count FROM charging_session WHERE id=? FOR UPDATE", sessionId);
        int status=((Number)s.get("status")).intValue();
        int attempts=((Number)s.get("recovery_count")).intValue();
        if (status!=ChargingSessionStatus.STARTING.code() && status!=ChargingSessionStatus.STOPPING.code() && status!=ChargingSessionStatus.RECOVERING.code()) return;
        int next=attempts+1; LocalDateTime now=LocalDateTime.now();
        if (next > maxAttempts) {
            jdbc.update("UPDATE charging_session SET status=?,recovery_count=?,last_recovery_time=?,update_time=? WHERE id=?",
                    ChargingSessionStatus.MANUAL_REVIEW.code(), next, now, now, sessionId);
            record(s,status,next,"MANUAL_REVIEW","MAX_ATTEMPTS","recovery exhausted");
            return;
        }
        jdbc.update("UPDATE charging_session SET status=?,recovery_count=?,last_recovery_time=?,update_time=? WHERE id=?",
                ChargingSessionStatus.RECOVERING.code(), next, now, now, sessionId);
        String commandId=UUID.randomUUID().toString();
        String payload=mapper.writeValueAsString(Map.of("sessionNo",String.valueOf(s.get("session_no")),"connectorNo",((Number)s.get("connector_no")).intValue()));
        jdbc.update("INSERT INTO device_command_outbox(id,tenant_id,command_id,device_id,connector_no,command_type,payload,status,retry_count,expire_time,create_time,update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                ids.nextId(), ((Number)s.get("tenant_id")).longValue(), commandId, String.valueOf(s.get("device_id")), ((Number)s.get("connector_no")).intValue(),
                "QUERY_TRANSACTION", payload, 0, 0, now.plusSeconds(60), now, now);
        record(s,status,next,"QUERY_TRANSACTION","ENQUEUED",commandId);
    }

    private void record(Map<String,Object> s,int original,int attempt,String action,String result,String detail) {
        jdbc.update("INSERT INTO charging_recovery_record(id,tenant_id,session_id,session_no,original_status,action_type,attempt_no,result_type,detail,create_time) VALUES (?,?,?,?,?,?,?,?,?,?)",
                ids.nextId(), ((Number)s.get("tenant_id")).longValue(), ((Number)s.get("id")).longValue(), String.valueOf(s.get("session_no")), original, action, attempt, result, detail, LocalDateTime.now());
    }
}
