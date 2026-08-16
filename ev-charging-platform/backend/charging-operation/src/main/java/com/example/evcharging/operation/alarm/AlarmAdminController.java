package com.example.evcharging.operation.alarm;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/operation/alarms")
public class AlarmAdminController {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    public AlarmAdminController(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    @GetMapping
    public ApiResponse<List<AlarmView>> list(@RequestParam(required=false) String status,@RequestParam(defaultValue="100") int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        String sql=status==null||status.isBlank()?"""
            SELECT alarm_no,device_id,connector_no,alarm_code,severity,status,metric_value,metric_unit,
                   occurrence_count,first_occurred_time,last_occurred_time,recovered_time,acknowledged_time
            FROM operation_alarm WHERE tenant_id=? ORDER BY id DESC LIMIT ?
            """:"""
            SELECT alarm_no,device_id,connector_no,alarm_code,severity,status,metric_value,metric_unit,
                   occurrence_count,first_occurred_time,last_occurred_time,recovered_time,acknowledged_time
            FROM operation_alarm WHERE tenant_id=? AND status=? ORDER BY id DESC LIMIT ?
            """;
        List<AlarmView> rows=(status==null||status.isBlank())
                ?jdbc.query(sql,(rs,n)->map(rs),tenant,size)
                :jdbc.query(sql,(rs,n)->map(rs),tenant,status.toUpperCase(),size);
        return ApiResponse.success(rows);
    }

    @PostMapping("/{alarmNo}/ack")
    public ApiResponse<Map<String,String>> acknowledge(@PathVariable String alarmNo){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();LocalDateTime now=LocalDateTime.now();
        int updated=jdbc.update("""
            UPDATE operation_alarm SET acknowledged_by=?,acknowledged_time=COALESCE(acknowledged_time,?),update_time=?
            WHERE tenant_id=? AND alarm_no=?
            """,user,now,now,tenant,alarmNo);
        return ApiResponse.success(Map.of("result",updated==1?"ACKNOWLEDGED":"NOT_FOUND"));
    }

    @PostMapping("/rules")
    public ApiResponse<Map<String,Long>> saveRule(@RequestBody SaveRuleRequest request){
        long tenant=RequestContext.requireTenantId();LocalDateTime now=LocalDateTime.now();
        List<Long> idsFound=jdbc.query("SELECT id FROM operation_alarm_rule WHERE tenant_id=? AND alarm_code=?",
                (rs,n)->rs.getLong(1),tenant,request.alarmCode());
        long id=idsFound.isEmpty()?ids.nextId():idsFound.get(0);
        if(idsFound.isEmpty()){
            jdbc.update("""
                INSERT INTO operation_alarm_rule(
                  id,tenant_id,alarm_code,enabled,min_severity,auto_work_order,sla_policy_id,create_time,update_time
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """,id,tenant,request.alarmCode(),request.enabled(),request.minSeverity(),request.autoWorkOrder(),
                    request.slaPolicyId(),now,now);
        }else{
            jdbc.update("""
                UPDATE operation_alarm_rule SET enabled=?,min_severity=?,auto_work_order=?,sla_policy_id=?,update_time=?
                WHERE id=? AND tenant_id=?
                """,request.enabled(),request.minSeverity(),request.autoWorkOrder(),request.slaPolicyId(),now,id,tenant);
        }
        return ApiResponse.success(Map.of("ruleId",id));
    }

    private AlarmView map(java.sql.ResultSet rs)throws java.sql.SQLException{
        return new AlarmView(rs.getString(1),rs.getString(2),(Integer)rs.getObject(3),rs.getString(4),rs.getString(5),
            rs.getString(6),rs.getString(7),rs.getString(8),rs.getInt(9),String.valueOf(rs.getObject(10)),
            String.valueOf(rs.getObject(11)),String.valueOf(rs.getObject(12)),String.valueOf(rs.getObject(13)));
    }

    public record SaveRuleRequest(String alarmCode,boolean enabled,String minSeverity,boolean autoWorkOrder,Long slaPolicyId){}
    public record AlarmView(String alarmNo,String deviceId,Integer connectorNo,String alarmCode,String severity,String status,
                            String metricValue,String metricUnit,int occurrenceCount,String firstOccurredTime,String lastOccurredTime,
                            String recoveredTime,String acknowledgedTime){}
}
