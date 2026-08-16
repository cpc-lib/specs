package com.example.evcharging.payment.application;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.DataScopeType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/merchant-api/v1/payments")
public class MerchantPaymentController {
    private final JdbcTemplate jdbc;
    public MerchantPaymentController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    public ApiResponse<List<View>> list(@RequestParam(defaultValue="100") int limit){
        var p=RequestContext.requirePrincipal();int size=Math.max(1,Math.min(limit,200));
        StringBuilder sql=new StringBuilder("""
            SELECT payment_no,biz_order_no,station_id,channel,amount_fen,status,channel_trade_no,create_time
            FROM payment_order WHERE tenant_id=?
            """);
        List<Object> args=new ArrayList<>();args.add(p.tenantId());
        applyStationScope(sql,args,p.dataScopeType(),p.stationIds());
        sql.append(" ORDER BY id DESC LIMIT ?");args.add(size);
        return ApiResponse.success(jdbc.query(sql.toString(),(rs,n)->new View(rs.getString(1),rs.getString(2),
                (Long)rs.getObject(3),rs.getString(4),rs.getLong(5),rs.getString(6),rs.getString(7),
                String.valueOf(rs.getObject(8))),args.toArray()));
    }

    static void applyStationScope(StringBuilder sql,List<Object> args,DataScopeType scope,Set<Long> stationIds){
        if(scope==DataScopeType.ALL||scope==DataScopeType.TENANT)return;
        if(scope==DataScopeType.STATION&&!stationIds.isEmpty()){
            sql.append(" AND station_id IN (").append(String.join(",",Collections.nCopies(stationIds.size(),"?"))).append(")");
            args.addAll(stationIds);return;
        }
        sql.append(" AND 1=0");
    }

    public record View(String paymentNo,String orderNo,Long stationId,String channel,long amountFen,
                       String status,String channelTradeNo,String createTime){}
}
