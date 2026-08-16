package com.example.evcharging.payment.application;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api/v1/payment-dashboard")
public class PaymentDashboardController {
    private final JdbcTemplate jdbc;
    public PaymentDashboardController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping public ApiResponse<Dashboard> dashboard(){
        long tenant=RequestContext.requireTenantId();
        int pending=jdbc.queryForObject("SELECT COUNT(*) FROM payment_order WHERE tenant_id=? AND status IN ('PENDING','UNKNOWN')",Integer.class,tenant);
        int success=jdbc.queryForObject("SELECT COUNT(*) FROM payment_order WHERE tenant_id=? AND status='SUCCESS' AND create_time>=CURDATE()",Integer.class,tenant);
        long paid=jdbc.queryForObject("SELECT COALESCE(SUM(amount_fen),0) FROM payment_order WHERE tenant_id=? AND status='SUCCESS' AND create_time>=CURDATE()",Long.class,tenant);
        return ApiResponse.success(new Dashboard(pending,success,paid));
    }
    public record Dashboard(int pendingOrUnknown,int todaySuccess,long todayPaidFen){}
}
