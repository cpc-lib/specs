package com.example.evcharging.operation;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api/v1/operation/dashboard")
public class OperationDashboardController {
    private final JdbcTemplate jdbc;
    public OperationDashboardController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    public ApiResponse<Dashboard> dashboard(){
        long tenant=RequestContext.requireTenantId();
        int active=jdbc.queryForObject("SELECT COUNT(*) FROM operation_alarm WHERE tenant_id=? AND status='ACTIVE'",Integer.class,tenant);
        int critical=jdbc.queryForObject("SELECT COUNT(*) FROM operation_alarm WHERE tenant_id=? AND status='ACTIVE' AND severity='CRITICAL'",Integer.class,tenant);
        int open=jdbc.queryForObject("SELECT COUNT(*) FROM operation_work_order WHERE tenant_id=? AND status NOT IN ('CLOSED','CANCELLED')",Integer.class,tenant);
        int breached=jdbc.queryForObject("SELECT COUNT(*) FROM operation_work_order WHERE tenant_id=? AND status NOT IN ('CLOSED','CANCELLED') AND sla_status='BREACHED'",Integer.class,tenant);
        return ApiResponse.success(new Dashboard(active,critical,open,breached));
    }
    public record Dashboard(int activeAlarms,int criticalAlarms,int openWorkOrders,int slaBreachedWorkOrders){}
}
