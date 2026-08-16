package com.example.evcharging.asset.station;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api/v1/assets/dashboard")
public class AssetDashboardController {
    private final JdbcTemplate jdbc;
    public AssetDashboardController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    public ApiResponse<Dashboard> dashboard(){
        long tenant=RequestContext.requireTenantId();
        int stations=jdbc.queryForObject("SELECT COUNT(*) FROM station WHERE tenant_id=? AND deleted=0",Integer.class,tenant);
        int chargers=jdbc.queryForObject("SELECT COUNT(*) FROM charger WHERE tenant_id=? AND deleted=0",Integer.class,tenant);
        int online=jdbc.queryForObject("SELECT COUNT(*) FROM charger WHERE tenant_id=? AND deleted=0 AND online_status=1",Integer.class,tenant);
        int connectors=jdbc.queryForObject("SELECT COUNT(*) FROM charger_connector WHERE tenant_id=? AND deleted=0",Integer.class,tenant);
        return ApiResponse.success(new Dashboard(stations,chargers,online,connectors));
    }
    public record Dashboard(int stations,int chargers,int onlineChargers,int connectors){}
}
