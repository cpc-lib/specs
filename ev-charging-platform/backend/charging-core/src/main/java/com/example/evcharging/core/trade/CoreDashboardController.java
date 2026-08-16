package com.example.evcharging.core.trade;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api/v1/core-dashboard")
public class CoreDashboardController {
    private final JdbcTemplate jdbc;
    public CoreDashboardController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping public ApiResponse<Dashboard> dashboard(){
        long tenant=RequestContext.requireTenantId();
        int active=jdbc.queryForObject("SELECT COUNT(*) FROM charging_session WHERE tenant_id=? AND status=30",Integer.class,tenant);
        int todayOrders=jdbc.queryForObject("SELECT COUNT(*) FROM charge_order WHERE tenant_id=? AND create_time>=CURDATE()",Integer.class,tenant);
        long energy=jdbc.queryForObject("SELECT COALESCE(SUM(energy_wh),0) FROM charge_order WHERE tenant_id=? AND create_time>=CURDATE()",Long.class,tenant);
        long revenue=jdbc.queryForObject("SELECT COALESCE(SUM(receivable_amount_fen),0) FROM charge_order WHERE tenant_id=? AND create_time>=CURDATE()",Long.class,tenant);
        return ApiResponse.success(new Dashboard(active,todayOrders,energy,revenue));
    }
    public record Dashboard(int activeCharging,int todayOrders,long todayEnergyWh,long todayRevenueFen){}
}
