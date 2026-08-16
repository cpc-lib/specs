package com.example.evcharging.operation.notification;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/operation/notifications")
public class NotificationAdminController {
    private final JdbcTemplate jdbc; private final IdGenerator ids;
    public NotificationAdminController(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    @PostMapping("/policies")
    public ApiResponse<Map<String,Long>> createPolicy(@RequestBody PolicyRequest r){
        long tenant=RequestContext.requireTenantId();
        if(r.delayMinutes()<0) throw new IllegalArgumentException("delayMinutes must be >= 0");
        long id=ids.nextId();LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            INSERT INTO operation_notification_policy(
              id,tenant_id,policy_code,trigger_type,min_severity,channel,delay_minutes,
              recipient_type,recipient_value,enabled,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """,id,tenant,r.policyCode(),r.triggerType(),r.minSeverity().toUpperCase(),
            r.channel().toUpperCase(),r.delayMinutes(),r.recipientType(),r.recipientValue(),true,now,now);
        return ApiResponse.success(Map.of("policyId",id));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<TaskView>> tasks(@RequestParam(defaultValue="100") int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        return ApiResponse.success(jdbc.query("""
            SELECT task_no,trigger_type,business_no,severity,channel,recipient,status,retry_count,
                   scheduled_time,sent_time,last_error
            FROM operation_notification_task WHERE tenant_id=? ORDER BY id DESC LIMIT ?
            """,(rs,n)->new TaskView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getString(7),rs.getInt(8),String.valueOf(rs.getObject(9)),
                String.valueOf(rs.getObject(10)),rs.getString(11)),tenant,size));
    }

    public record PolicyRequest(String policyCode,String triggerType,String minSeverity,String channel,
                                int delayMinutes,String recipientType,String recipientValue){}
    public record TaskView(String taskNo,String triggerType,String businessNo,String severity,String channel,
                           String recipient,String status,int retryCount,String scheduledTime,String sentTime,String lastError){}
}
