package com.example.evcharging.operation.alarm;

import com.example.evcharging.framework.contract.DeviceAlarmEvent;
import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.operation.workorder.WorkOrderCreationService;
import com.example.evcharging.operation.notification.NotificationEscalationService;
import com.example.evcharging.operation.asset.DeviceStationResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Component
public class AlarmEventConsumer {
    private static final String CONSUMER="operation-alarm-v1";
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final IdGenerator ids;
    private final WorkOrderCreationService workOrders;
    private final NotificationEscalationService notifications;
    private final DeviceStationResolver stationResolver;

    public AlarmEventConsumer(JdbcTemplate jdbc,ObjectMapper mapper,IdGenerator ids,
                              WorkOrderCreationService workOrders,NotificationEscalationService notifications,
                              DeviceStationResolver stationResolver){
        this.jdbc=jdbc;this.mapper=mapper;this.ids=ids;this.workOrders=workOrders;this.notifications=notifications;
        this.stationResolver=stationResolver;
    }

    @KafkaListener(topics="${charging.operation.alarm-topic:ev.device.alarm.v1}",groupId="charging-operation-alarm-v1")
    @Transactional
    public void consume(String raw)throws Exception{
        DomainEventEnvelope<DeviceAlarmEvent> event=mapper.readValue(raw,new TypeReference<DomainEventEnvelope<DeviceAlarmEvent>>(){});
        if(!markInbox(event)) return;
        DeviceAlarmEvent payload=Objects.requireNonNull(event.payload(),"alarm payload required");
        if(payload.tenantId()!=event.tenantId()) throw new IllegalArgumentException("tenant mismatch");
        if("RAISED".equalsIgnoreCase(payload.eventType())) raise(event.eventId(),payload);
        else if("RECOVERED".equalsIgnoreCase(payload.eventType())) recover(event.eventId(),payload);
        else throw new IllegalArgumentException("unsupported alarm event type: "+payload.eventType());
    }

    private boolean markInbox(DomainEventEnvelope<?> e){
        try{
            jdbc.update("INSERT INTO operation_event_inbox(id,consumer_name,event_id,event_type,processed_time) VALUES (?,?,?,?,?)",
                    ids.nextId(),CONSUMER,e.eventId(),e.eventType(),LocalDateTime.now());
            return true;
        }catch(DuplicateKeyException duplicate){return false;}
    }

    private void raise(String eventId,DeviceAlarmEvent e){
        String fingerprint=AlarmFingerprint.of(e.deviceId(),e.connectorNo(),e.alarmCode());
        AlarmSeverity incoming=AlarmSeverity.parse(e.severity());
        LocalDateTime occurred=LocalDateTime.ofInstant(e.occurredAt()!=null?e.occurredAt():Instant.now(),ZoneOffset.UTC);

        List<Long> active=findActive(e.tenantId(),fingerprint);
        if(!active.isEmpty()){
            long alarmId=active.get(0);
            ExistingAlarm current=jdbc.queryForObject("""
                SELECT alarm_no,severity,device_id,alarm_code FROM operation_alarm WHERE id=?
                """,(rs,n)->new ExistingAlarm(rs.getString(1),AlarmSeverity.parse(rs.getString(2)),rs.getString(3),rs.getString(4)),alarmId);
            AlarmSeverity max=AlarmSeverity.max(current.severity(),incoming);
            jdbc.update("""
                UPDATE operation_alarm
                SET severity=?,metric_value=?,metric_unit=?,alarm_message=?,occurrence_count=occurrence_count+1,
                    last_occurred_time=?,update_time=?
                WHERE id=? AND status='ACTIVE'
                """,max.name(),e.metricValue(),e.metricUnit(),e.message(),occurred,LocalDateTime.now(),alarmId);
            insertOccurrence(e.tenantId(),alarmId,eventId,"RAISED",incoming,e.metricValue(),occurred);
            // A WARNING may later escalate to MAJOR/CRITICAL. Re-evaluate automation idempotently.
            workOrders.createForAlarm(e.tenantId(),alarmId,current.alarmNo(),current.deviceId(),current.alarmCode(),max);
            notifications.schedule(e.tenantId(),"ALARM_RAISED",current.alarmNo(),max,
                    current.alarmCode()+" on "+current.deviceId()+" severity="+max.name());
            return;
        }

        long alarmId=ids.nextId();String alarmNo="AL"+alarmId;LocalDateTime now=LocalDateTime.now();
        Long stationId=stationResolver.resolve(e.deviceId());
        try{
            jdbc.update("INSERT INTO operation_active_alarm(tenant_id,fingerprint,alarm_id,create_time) VALUES (?,?,?,?)",
                    e.tenantId(),fingerprint,alarmId,now);
        }catch(DuplicateKeyException race){
            long winner=findActive(e.tenantId(),fingerprint).get(0);
            jdbc.update("""
                UPDATE operation_alarm SET occurrence_count=occurrence_count+1,last_occurred_time=?,update_time=? WHERE id=?
                """,occurred,now,winner);
            insertOccurrence(e.tenantId(),winner,eventId,"RAISED",incoming,e.metricValue(),occurred);
            return;
        }

        jdbc.update("""
            INSERT INTO operation_alarm(
              id,tenant_id,alarm_no,fingerprint,device_id,station_id,connector_no,alarm_code,severity,status,
              metric_value,metric_unit,alarm_message,occurrence_count,first_occurred_time,last_occurred_time,
              create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,?, 'ACTIVE',?,?,?,?,?,?,?,?)
            """,alarmId,e.tenantId(),alarmNo,fingerprint,e.deviceId(),stationId,e.connectorNo(),e.alarmCode(),incoming.name(),
                e.metricValue(),e.metricUnit(),e.message(),1,occurred,occurred,now,now);
        insertOccurrence(e.tenantId(),alarmId,eventId,"RAISED",incoming,e.metricValue(),occurred);
        workOrders.createForAlarm(e.tenantId(),alarmId,alarmNo,e.deviceId(),e.alarmCode(),incoming);
        notifications.schedule(e.tenantId(),"ALARM_RAISED",alarmNo,incoming,
                e.alarmCode()+" on "+e.deviceId()+" severity="+incoming.name());
    }

    private void recover(String eventId,DeviceAlarmEvent e){
        String fingerprint=AlarmFingerprint.of(e.deviceId(),e.connectorNo(),e.alarmCode());
        List<Long> active=findActive(e.tenantId(),fingerprint);
        if(active.isEmpty()) return;
        long alarmId=active.get(0);
        LocalDateTime occurred=LocalDateTime.ofInstant(e.occurredAt()!=null?e.occurredAt():Instant.now(),ZoneOffset.UTC);
        jdbc.update("""
            UPDATE operation_alarm
            SET status='RECOVERED',recovered_time=?,last_occurred_time=?,update_time=?
            WHERE id=? AND status='ACTIVE'
            """,occurred,occurred,LocalDateTime.now(),alarmId);
        jdbc.update("DELETE FROM operation_active_alarm WHERE tenant_id=? AND fingerprint=? AND alarm_id=?",
                e.tenantId(),fingerprint,alarmId);
        insertOccurrence(e.tenantId(),alarmId,eventId,"RECOVERED",AlarmSeverity.INFO,null,occurred);
    }

    private record ExistingAlarm(String alarmNo,AlarmSeverity severity,String deviceId,String alarmCode){}

    private List<Long> findActive(long tenant,String fingerprint){
        return jdbc.query("SELECT alarm_id FROM operation_active_alarm WHERE tenant_id=? AND fingerprint=?",
                (rs,n)->rs.getLong(1),tenant,fingerprint);
    }

    private void insertOccurrence(long tenant,long alarmId,String eventId,String type,AlarmSeverity severity,String value,LocalDateTime occurred){
        jdbc.update("""
            INSERT INTO operation_alarm_occurrence(
              id,tenant_id,alarm_id,event_id,event_type,severity,metric_value,occurred_time,create_time
            ) VALUES (?,?,?,?,?,?,?,?,?)
            """,ids.nextId(),tenant,alarmId,eventId,type,severity.name(),value,occurred,LocalDateTime.now());
    }
}
