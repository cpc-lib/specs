package com.example.evcharging.operation.sla;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/operation/sla")
public class SlaAdminController {
    private final JdbcTemplate jdbc;private final IdGenerator ids;
    public SlaAdminController(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    @PostMapping("/policies")
    public ApiResponse<Map<String,Long>> create(@RequestBody CreatePolicyRequest r){
        long tenant=RequestContext.requireTenantId();new SlaPolicy(r.responseMinutes(),r.resolutionMinutes());
        long id=ids.nextId();LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            INSERT INTO operation_sla_policy(
              id,tenant_id,policy_code,policy_name,severity,response_minutes,resolution_minutes,enabled,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,?,?)
            """,id,tenant,r.policyCode(),r.policyName(),r.severity().toUpperCase(),r.responseMinutes(),r.resolutionMinutes(),true,now,now);
        return ApiResponse.success(Map.of("policyId",id));
    }

    @GetMapping("/breaches")
    public ApiResponse<List<BreachView>> breaches(){
        long tenant=RequestContext.requireTenantId();
        return ApiResponse.success(jdbc.query("""
            SELECT work_order_no,breach_type,due_time,detected_time,acknowledged
            FROM operation_sla_breach WHERE tenant_id=? ORDER BY id DESC LIMIT 200
            """,(rs,n)->new BreachView(rs.getString(1),rs.getString(2),String.valueOf(rs.getObject(3)),
                String.valueOf(rs.getObject(4)),rs.getBoolean(5)),tenant));
    }

    public record CreatePolicyRequest(String policyCode,String policyName,String severity,int responseMinutes,int resolutionMinutes){}
    public record BreachView(String workOrderNo,String breachType,String dueTime,String detectedTime,boolean acknowledged){}
}
