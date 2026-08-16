package com.example.evcharging.core.charging.application;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal-api/v1/core/partner")
public class InternalPartnerChargingController {
    private final ChargingApplicationService charging;
    private final JdbcTemplate jdbc;

    public InternalPartnerChargingController(ChargingApplicationService charging,JdbcTemplate jdbc){
        this.charging=charging;this.jdbc=jdbc;
    }

    @PostMapping("/charging/start")
    public ChargingSessionView start(@RequestBody PartnerStartCommand c){
        return charging.startForUser(RequestContext.requireTenantId(),c.localUserId(),
                new StartChargingRequest(c.requestId(),c.connectorCode(),null));
    }

    @GetMapping("/charging/{sessionNo}")
    public ChargingSessionView session(@PathVariable String sessionNo,@RequestParam long localUserId){
        return charging.viewForUser(RequestContext.requireTenantId(),localUserId,sessionNo);
    }

    @PostMapping("/charging/{sessionNo}/stop")
    public ChargingSessionView stop(@PathVariable String sessionNo,@RequestBody PartnerStopCommand c){
        return charging.stopForUser(RequestContext.requireTenantId(),c.localUserId(),sessionNo,c.requestId());
    }

    @GetMapping("/orders/{orderNo}")
    public PartnerOrderSnapshot order(@PathVariable String orderNo){
        long tenant=RequestContext.requireTenantId();
        return jdbc.queryForObject("""
            SELECT order_no,session_no,user_id,station_id,energy_wh,receivable_amount_fen,paid_amount_fen,
                   trade_status,payment_status,finish_time
            FROM charge_order WHERE tenant_id=? AND order_no=?
            """,(rs,n)->new PartnerOrderSnapshot(rs.getString(1),rs.getString(2),rs.getLong(3),rs.getLong(4),
                rs.getLong(5),rs.getLong(6),rs.getLong(7),rs.getInt(8),rs.getInt(9),String.valueOf(rs.getObject(10))),
                tenant,orderNo);
    }

    public record PartnerStartCommand(long localUserId,String requestId,String connectorCode){}
    public record PartnerStopCommand(long localUserId,String requestId){}
    public record PartnerOrderSnapshot(String orderNo,String sessionNo,long localUserId,long stationId,long energyWh,
                                       long receivableAmountFen,long paidAmountFen,int tradeStatus,int paymentStatus,String finishTime){}
}
