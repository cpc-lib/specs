package com.example.evcharging.operation.alarm;

import com.example.evcharging.operation.sla.SlaPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmRuleDecisionService {
    private final JdbcTemplate jdbc;
    public AlarmRuleDecisionService(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public Decision decide(long tenantId,String alarmCode,AlarmSeverity severity){
        List<ConfiguredRule> configured=jdbc.query("""
            SELECT enabled,auto_work_order,sla_policy_id,min_severity
            FROM operation_alarm_rule
            WHERE tenant_id=? AND alarm_code=?
            """,(rs,n)->new ConfiguredRule(
                rs.getBoolean(1),
                rs.getBoolean(2),
                (Long)rs.getObject(3),
                AlarmSeverity.parse(rs.getString(4))
            ),tenantId,alarmCode);
        if(!configured.isEmpty()){
            ConfiguredRule d=configured.get(0);
            boolean auto=d.enabled() && d.autoWorkOrder() && severity.rank()>=d.minSeverity().rank();
            return new Decision(auto,d.slaPolicyId(),d.minSeverity());
        }
        if("DEVICE_OFFLINE".equalsIgnoreCase(alarmCode)) {
            return new Decision(false,null,AlarmSeverity.MAJOR);
        }
        return new Decision(severity.rank()>=AlarmSeverity.MAJOR.rank(),null,AlarmSeverity.MAJOR);
    }

    public SlaPolicy resolveSla(long tenantId,Long policyId,AlarmSeverity severity){
        if(policyId!=null){
            List<SlaPolicy> rows=jdbc.query("""
                SELECT response_minutes,resolution_minutes
                FROM operation_sla_policy WHERE tenant_id=? AND id=? AND enabled=1
                """,(rs,n)->new SlaPolicy(rs.getInt(1),rs.getInt(2)),tenantId,policyId);
            if(!rows.isEmpty()) return rows.get(0);
        }
        List<SlaPolicy> rows=jdbc.query("""
            SELECT response_minutes,resolution_minutes
            FROM operation_sla_policy
            WHERE tenant_id=? AND severity=? AND enabled=1
            ORDER BY id DESC LIMIT 1
            """,(rs,n)->new SlaPolicy(rs.getInt(1),rs.getInt(2)),tenantId,severity.name());
        if(!rows.isEmpty()) return rows.get(0);
        return switch(severity){
            case CRITICAL -> new SlaPolicy(10,120);
            case MAJOR -> new SlaPolicy(30,240);
            case WARNING -> new SlaPolicy(120,720);
            case INFO -> new SlaPolicy(240,1440);
        };
    }

    private record ConfiguredRule(boolean enabled,boolean autoWorkOrder,Long slaPolicyId,AlarmSeverity minSeverity){}
    public record Decision(boolean autoWorkOrder,Long slaPolicyId,AlarmSeverity minSeverity){}
}
