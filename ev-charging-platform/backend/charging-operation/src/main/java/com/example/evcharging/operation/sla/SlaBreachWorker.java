package com.example.evcharging.operation.sla;

import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.operation.workorder.WorkOrderCreationService;
import com.example.evcharging.operation.notification.NotificationEscalationService;
import com.example.evcharging.operation.alarm.AlarmSeverity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaBreachWorker {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final WorkOrderCreationService events;
    private final NotificationEscalationService notifications;

    public SlaBreachWorker(JdbcTemplate jdbc,IdGenerator ids,WorkOrderCreationService events,
                           NotificationEscalationService notifications){
        this.jdbc=jdbc;this.ids=ids;this.events=events;this.notifications=notifications;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void scanOne(long id){
        List<Row> rows=jdbc.query("""
            SELECT id,tenant_id,work_order_no,priority,first_response_time,response_due_time,resolution_due_time,status
            FROM operation_work_order WHERE id=? FOR UPDATE
            """,(rs,n)->new Row(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),
                rs.getObject(5,LocalDateTime.class),rs.getObject(6,LocalDateTime.class),
                rs.getObject(7,LocalDateTime.class),rs.getString(8)),id);
        if(rows.isEmpty()) return;
        Row r=rows.get(0);
        if("CLOSED".equals(r.status())||"CANCELLED".equals(r.status())) return;

        LocalDateTime now=LocalDateTime.now();
        boolean breached=false;
        if(r.firstResponse()==null&&r.responseDue().isBefore(now)){
            breached|=record(r,"RESPONSE",r.responseDue(),now);
        }
        if(r.resolutionDue().isBefore(now)){
            breached|=record(r,"RESOLUTION",r.resolutionDue(),now);
        }
        if(breached){
            jdbc.update("UPDATE operation_work_order SET sla_status='BREACHED',update_time=? WHERE id=?",now,id);
        }
    }

    private boolean record(Row r,String type,LocalDateTime due,LocalDateTime now){
        try{
            jdbc.update("""
                INSERT INTO operation_sla_breach(
                  id,tenant_id,work_order_id,work_order_no,breach_type,due_time,detected_time,acknowledged
                ) VALUES (?,?,?,?,?,?,?,0)
                """,ids.nextId(),r.tenantId(),r.id(),r.workOrderNo(),type,due,now);
            events.appendEvent(r.tenantId(),r.id(),r.workOrderNo(),"SLA_"+type+"_BREACHED",null);
            notifications.schedule(r.tenantId(),"SLA_"+type+"_BREACH",r.workOrderNo(),
                    AlarmSeverity.parse(r.priority()),"SLA "+type+" breached for "+r.workOrderNo());
            return true;
        }catch(DuplicateKeyException duplicate){
            return false;
        }
    }

    private record Row(long id,long tenantId,String workOrderNo,String priority,LocalDateTime firstResponse,
                       LocalDateTime responseDue,LocalDateTime resolutionDue,String status){}
}
