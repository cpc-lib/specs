package com.example.evcharging.core.trade;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal-api/v1/core/regulatory")
public class InternalRegulatoryOrderController {
    private final JdbcTemplate jdbc;
    public InternalRegulatoryOrderController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/orders/latest")
    public List<OrderView> latest(@RequestParam(defaultValue="500") int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,1000));
        return jdbc.query("""
            SELECT order_no,session_no,station_id,energy_wh,energy_amount_fen,service_amount_fen,
                   receivable_amount_fen,paid_amount_fen,trade_status,payment_status,finish_time,update_time
            FROM charge_order WHERE tenant_id=? ORDER BY update_time DESC,id DESC LIMIT ?
            """,(rs,n)->new OrderView(rs.getString(1),rs.getString(2),rs.getLong(3),rs.getLong(4),
                rs.getLong(5),rs.getLong(6),rs.getLong(7),rs.getLong(8),rs.getInt(9),rs.getInt(10),
                String.valueOf(rs.getObject(11)),String.valueOf(rs.getObject(12))),tenant,size);
    }

    public record OrderView(String orderNo,String sessionNo,long stationId,long energyWh,long energyAmountFen,
                            long serviceAmountFen,long receivableAmountFen,long paidAmountFen,
                            int tradeStatus,int paymentStatus,String finishTime,String updateTime){}
}
