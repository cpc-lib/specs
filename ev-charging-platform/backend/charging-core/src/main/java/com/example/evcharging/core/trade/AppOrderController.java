package com.example.evcharging.core.trade;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/app-api/v1/orders")
public class AppOrderController {
    private final JdbcTemplate jdbc;
    public AppOrderController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    public ApiResponse<List<OrderView>> list(@RequestParam(defaultValue="50") int limit){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        int size=Math.max(1,Math.min(limit,100));
        return ApiResponse.success(jdbc.query("""
            SELECT order_no,session_no,station_id,energy_wh,receivable_amount_fen,paid_amount_fen,
                   trade_status,payment_status,create_time
            FROM charge_order WHERE tenant_id=? AND user_id=?
            ORDER BY id DESC LIMIT ?
            """,(rs,n)->new OrderView(rs.getString(1),rs.getString(2),rs.getLong(3),rs.getLong(4),
                rs.getLong(5),rs.getLong(6),rs.getInt(7),rs.getInt(8),String.valueOf(rs.getObject(9))),tenant,user,size));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderView> detail(@PathVariable String orderNo){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        OrderView order=jdbc.queryForObject("""
            SELECT order_no,session_no,station_id,energy_wh,receivable_amount_fen,paid_amount_fen,
                   trade_status,payment_status,create_time
            FROM charge_order WHERE tenant_id=? AND user_id=? AND order_no=?
            """,(rs,n)->new OrderView(rs.getString(1),rs.getString(2),rs.getLong(3),rs.getLong(4),
                rs.getLong(5),rs.getLong(6),rs.getInt(7),rs.getInt(8),String.valueOf(rs.getObject(9))),tenant,user,orderNo);
        return ApiResponse.success(order);
    }

    public record OrderView(String orderNo,String sessionNo,long stationId,long energyWh,long receivableAmountFen,
                            long paidAmountFen,int tradeStatus,int paymentStatus,String createTime){}
}
