package com.example.evcharging.finance.settlement;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.DataScopeType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/merchant-api/v1/finance")
public class MerchantFinanceController {
    private final JdbcTemplate jdbc;
    public MerchantFinanceController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/settlements")
    public ApiResponse<List<View>> settlements(@RequestParam(defaultValue="100") int limit){
        var p=RequestContext.requirePrincipal();int size=Math.max(1,Math.min(limit,200));
        StringBuilder sql=new StringBuilder("""
            SELECT s.source_no,s.payment_no,s.station_id,s.business_date,s.status,s.settlement_base_amount_fen,
                   b.batch_no,b.completed_time
            FROM finance_settlement_source s
            LEFT JOIN finance_settlement_order o ON o.source_id=s.id
            LEFT JOIN finance_settlement_batch b ON b.id=o.batch_id
            WHERE s.tenant_id=?
            """);
        List<Object> args=new ArrayList<>();args.add(p.tenantId());
        applyScope(sql,args,p.dataScopeType(),p.stationIds());
        sql.append(" ORDER BY s.id DESC LIMIT ?");args.add(size);
        return ApiResponse.success(jdbc.query(sql.toString(),(rs,n)->new View(
                rs.getString(1),rs.getString(2),(Long)rs.getObject(3),String.valueOf(rs.getObject(4)),
                rs.getString(5),rs.getLong(6),rs.getString(7),String.valueOf(rs.getObject(8))),args.toArray()));
    }

    private static void applyScope(StringBuilder sql,List<Object> args,DataScopeType scope,Set<Long> stationIds){
        if(scope==DataScopeType.ALL||scope==DataScopeType.TENANT)return;
        if(scope==DataScopeType.STATION&&!stationIds.isEmpty()){
            sql.append(" AND s.station_id IN (").append(String.join(",",Collections.nCopies(stationIds.size(),"?"))).append(")");
            args.addAll(stationIds);return;
        }
        sql.append(" AND 1=0");
    }

    public record View(String sourceNo,String paymentNo,Long stationId,String businessDate,String status,
                       long amountFen,String batchNo,String completedTime){}
}
