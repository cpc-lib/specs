package com.example.evcharging.finance.reconciliation;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/v1/finance/reconciliation-schedules")
public class ReconciliationScheduleAdminController {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    public ReconciliationScheduleAdminController(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    public record SaveRequest(String channel,String merchantId,String zoneId,boolean enabled){}
    public record ScheduleView(String channel,String merchantId,String zoneId,boolean enabled,String lastSuccessBusinessDate){}

    @PostMapping
    public ApiResponse<Map<String,String>> save(@RequestBody SaveRequest request){
        long tenant=RequestContext.requireTenantId();
        String channel=required(request.channel(),"channel").toUpperCase();
        String merchant=request.merchantId()==null||request.merchantId().isBlank()?"DEFAULT":request.merchantId().trim();
        String zone=request.zoneId()==null||request.zoneId().isBlank()?"Asia/Shanghai":request.zoneId().trim();
        java.time.ZoneId.of(zone); // validation
        LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            INSERT INTO finance_reconciliation_schedule(
              id,tenant_id,channel,merchant_id,zone_id,enabled,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?)
            ON DUPLICATE KEY UPDATE zone_id=VALUES(zone_id),enabled=VALUES(enabled),update_time=VALUES(update_time)
            """,ids.nextId(),tenant,channel,merchant,zone,request.enabled(),now,now);
        return ApiResponse.ok(Map.of("status","SAVED"));
    }

    @GetMapping
    public ApiResponse<List<ScheduleView>> list(){
        long tenant=RequestContext.requireTenantId();
        return ApiResponse.ok(jdbc.query("""
            SELECT channel,merchant_id,zone_id,enabled,last_success_business_date
            FROM finance_reconciliation_schedule WHERE tenant_id=? ORDER BY id
            """,(rs,n)->new ScheduleView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getBoolean(4),String.valueOf(rs.getObject(5))),tenant));
    }

    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" required");return v.trim();}
}
