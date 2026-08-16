package com.example.evcharging.core.charging.application;

import com.example.evcharging.core.asset.ConnectorSnapshot;
import com.example.evcharging.core.billing.application.BillingSnapshotResolver;
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
public class ChargingStartTransaction {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final BillingSnapshotResolver billingSnapshots;
    private final ObjectMapper mapper;

    public ChargingStartTransaction(JdbcTemplate jdbc, IdGenerator ids, BillingSnapshotResolver billingSnapshots, ObjectMapper mapper) {
        this.jdbc=jdbc; this.ids=ids; this.billingSnapshots=billingSnapshots; this.mapper=mapper;
    }

    @Transactional
    public String create(long tenantId,long userId,StartChargingRequest request,ConnectorSnapshot connector) {
        long id=ids.nextId(); String sessionNo="CS"+id; LocalDateTime now=LocalDateTime.now();
        try {
            jdbc.update("INSERT INTO api_idempotency(id,tenant_id,operation_type,request_id,resource_no,create_time) VALUES (?,?,?,?,?,?)",
                    ids.nextId(),tenantId,"START_CHARGING",request.requestId(),sessionNo,now);
        } catch(DuplicateKeyException duplicate) {
            return jdbc.queryForObject("SELECT resource_no FROM api_idempotency WHERE tenant_id=? AND operation_type='START_CHARGING' AND request_id=?",String.class,tenantId,request.requestId());
        }
        try {
            jdbc.update("INSERT INTO connector_active_session(connector_id,tenant_id,session_id,session_no,user_id,create_time) VALUES (?,?,?,?,?,?)",
                    connector.connectorId(),tenantId,id,sessionNo,userId,now);
        } catch(DuplicateKeyException ex) { throw new IllegalStateException("active session exists",ex); }
        long snapshotId=billingSnapshots.resolveAndPersist(tenantId,connector.stationId(),id).snapshotId();
        jdbc.update("INSERT INTO charging_session(id,tenant_id,operator_id,session_no,user_id,vehicle_id,station_id,charger_id,connector_id,device_id,connector_no,status,billing_snapshot_id,version,create_time,update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id,tenantId,0,sessionNo,userId,request.vehicleId(),connector.stationId(),connector.chargerId(),connector.connectorId(),connector.deviceId(),connector.connectorNo(),ChargingSessionStatus.STARTING.code(),snapshotId,0,now,now);
        String commandId=UUID.randomUUID().toString();
        String payload=json(Map.of("sessionNo",sessionNo,"connectorNo",connector.connectorNo()));
        jdbc.update("INSERT INTO device_command_outbox(id,tenant_id,command_id,device_id,connector_no,command_type,payload,status,retry_count,expire_time,create_time,update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                ids.nextId(),tenantId,commandId,connector.deviceId(),connector.connectorNo(),"START_CHARGING",payload,0,0,now.plusSeconds(60),now,now);
        return sessionNo;
    }

    private String json(Object value){try{return mapper.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException("cannot serialize device command",e);}}
}
