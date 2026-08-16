package com.example.evcharging.core.charging.application;

import com.example.evcharging.core.charging.domain.ChargingSessionStatus;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ChargingStopTransaction {
    private final JdbcTemplate jdbc; private final IdGenerator ids; private final ObjectMapper mapper;
    public ChargingStopTransaction(JdbcTemplate jdbc,IdGenerator ids,ObjectMapper mapper){this.jdbc=jdbc;this.ids=ids;this.mapper=mapper;}

    @Transactional
    public String stop(long tenantId,String sessionNo,String requestId){
        Map<String,Object> row=jdbc.queryForMap("SELECT status,connector_id,connector_no,device_id FROM charging_session WHERE tenant_id=? AND session_no=? FOR UPDATE",tenantId,sessionNo);
        int status=((Number)row.get("status")).intValue();
        if(status==ChargingSessionStatus.FINISHED.code()||status==ChargingSessionStatus.STOPPING.code())return sessionNo;
        String operation="STOP_CHARGING:"+sessionNo;LocalDateTime now=LocalDateTime.now();
        try{jdbc.update("INSERT INTO api_idempotency(id,tenant_id,operation_type,request_id,resource_no,create_time) VALUES (?,?,?,?,?,?)",ids.nextId(),tenantId,operation,requestId,sessionNo,now);}
        catch(DuplicateKeyException duplicate){return sessionNo;}
        int changed=jdbc.update("UPDATE charging_session SET status=?,update_time=? WHERE tenant_id=? AND session_no=? AND status IN (?,?,?,?)",
                ChargingSessionStatus.STOPPING.code(),now,tenantId,sessionNo,ChargingSessionStatus.STARTING.code(),ChargingSessionStatus.PREPARING.code(),ChargingSessionStatus.CHARGING.code(),ChargingSessionStatus.RECOVERING.code());
        if(changed==0)return sessionNo;
        String commandId=UUID.randomUUID().toString();String payload=json(Map.of("sessionNo",sessionNo,"connectorNo",((Number)row.get("connector_no")).intValue()));
        jdbc.update("INSERT INTO device_command_outbox(id,tenant_id,command_id,device_id,connector_no,command_type,payload,status,retry_count,expire_time,create_time,update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                ids.nextId(),tenantId,commandId,String.valueOf(row.get("device_id")),((Number)row.get("connector_no")).intValue(),"STOP_CHARGING",payload,0,0,now.plusSeconds(60),now,now);
        return sessionNo;
    }
    private String json(Object value){try{return mapper.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException("cannot serialize stop command",e);}}
}
