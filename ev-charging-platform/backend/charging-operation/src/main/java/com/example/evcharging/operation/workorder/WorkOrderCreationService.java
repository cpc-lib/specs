package com.example.evcharging.operation.workorder;

import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.operation.alarm.*;
import com.example.evcharging.operation.sla.SlaPolicy;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WorkOrderCreationService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final RuntimeService runtimeService;
    private final AlarmRuleDecisionService rules;

    public WorkOrderCreationService(JdbcTemplate jdbc,IdGenerator ids,RuntimeService runtimeService,AlarmRuleDecisionService rules){
        this.jdbc=jdbc;this.ids=ids;this.runtimeService=runtimeService;this.rules=rules;
    }

    public String createForAlarm(long tenantId,long alarmId,String alarmNo,String deviceId,String alarmCode,AlarmSeverity severity){
        List<String> existing=jdbc.query("""
            SELECT work_order_no FROM operation_work_order WHERE tenant_id=? AND alarm_id=?
            """,(rs,n)->rs.getString(1),tenantId,alarmId);
        if(!existing.isEmpty()) return existing.get(0);

        var decision=rules.decide(tenantId,alarmCode,severity);
        if(!decision.autoWorkOrder()) return null;
        SlaPolicy.DueTimes due=rules.resolveSla(tenantId,decision.slaPolicyId(),severity).dueFrom(LocalDateTime.now());

        Long stationId=jdbc.queryForObject("SELECT station_id FROM operation_alarm WHERE tenant_id=? AND id=?",
                Long.class,tenantId,alarmId);
        long id=ids.nextId();String no="WO"+id;LocalDateTime now=LocalDateTime.now();
        try{
            jdbc.update("""
                INSERT INTO operation_work_order(
                  id,tenant_id,work_order_no,alarm_id,alarm_no,station_id,title,priority,status,
                  response_due_time,resolution_due_time,sla_status,create_time,update_time
                ) VALUES (?,?,?,?,?,?,?,?,'PENDING_ASSIGNMENT',?,?,'NORMAL',?,?)
                """,id,tenantId,no,alarmId,alarmNo,stationId,alarmCode+" on "+deviceId,severity.name(),
                    due.responseDueTime(),due.resolutionDueTime(),now,now);
        }catch(DuplicateKeyException duplicate){
            return jdbc.queryForObject("SELECT work_order_no FROM operation_work_order WHERE tenant_id=? AND alarm_id=?",
                    String.class,tenantId,alarmId);
        }

        ProcessInstance process=runtimeService.startProcessInstanceByKey(
                "maintenanceWorkOrder",no,Map.of("workOrderNo",no,"tenantId",tenantId));
        jdbc.update("UPDATE operation_work_order SET process_instance_id=?,update_time=? WHERE id=?",
                process.getProcessInstanceId(),LocalDateTime.now(),id);
        appendEvent(tenantId,id,no,"CREATED",null);
        return no;
    }

    public void appendEvent(long tenantId,long workOrderId,String workOrderNo,String type,Long operatorUserId){
        jdbc.update("""
            INSERT INTO operation_work_order_event(
              id,tenant_id,work_order_id,work_order_no,event_type,operator_user_id,event_payload,occurred_time
            ) VALUES (?,?,?,?,?,?,NULL,?)
            """,ids.nextId(),tenantId,workOrderId,workOrderNo,type,operatorUserId,LocalDateTime.now());
    }
}
