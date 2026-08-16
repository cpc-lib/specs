package com.example.evcharging.operation.notification;

import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.operation.alarm.AlarmSeverity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationEscalationService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public NotificationEscalationService(JdbcTemplate jdbc,IdGenerator ids){
        this.jdbc=jdbc;this.ids=ids;
    }

    public void schedule(long tenantId,String triggerType,String businessNo,AlarmSeverity severity,String content){
        List<Policy> policies=jdbc.query("""
            SELECT id,channel,delay_minutes,recipient_value,min_severity
            FROM operation_notification_policy
            WHERE tenant_id=? AND trigger_type=? AND enabled=1
            ORDER BY delay_minutes,id
            """,(rs,n)->new Policy(rs.getLong(1),rs.getString(2),rs.getInt(3),rs.getString(4),
                AlarmSeverity.parse(rs.getString(5))),tenantId,triggerType);

        boolean created=false;
        for(Policy p:policies){
            if(severity.rank()<p.minSeverity().rank()) continue;
            insertTask(tenantId,p.id(),triggerType,businessNo,severity,p.channel(),p.recipient(),
                    content,LocalDateTime.now().plusMinutes(p.delayMinutes()));
            created=true;
        }

        // Safe development fallback: persist one app/on-call task for unconfigured CRITICAL incidents.
        if(!created && severity==AlarmSeverity.CRITICAL){
            insertTask(tenantId,0L,triggerType,businessNo,severity,"APP","ON_CALL",
                    content,LocalDateTime.now());
        }
    }

    private void insertTask(long tenantId,long policyId,String triggerType,String businessNo,
                            AlarmSeverity severity,String channel,String recipient,String content,
                            LocalDateTime scheduledTime){
        long id=ids.nextId();LocalDateTime now=LocalDateTime.now();
        try{
            jdbc.update("""
                INSERT INTO operation_notification_task(
                  id,tenant_id,task_no,policy_id,trigger_type,business_no,severity,channel,recipient,
                  content,scheduled_time,status,retry_count,create_time,update_time
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,'PENDING',0,?,?)
                """,id,tenantId,"NT"+id,policyId,triggerType,businessNo,severity.name(),
                channel.toUpperCase(),recipient,content,scheduledTime,now,now);
        }catch(DuplicateKeyException duplicate){
            // Same trigger/business/policy is intentionally idempotent.
        }
    }

    private record Policy(long id,String channel,int delayMinutes,String recipient,AlarmSeverity minSeverity){}
}
