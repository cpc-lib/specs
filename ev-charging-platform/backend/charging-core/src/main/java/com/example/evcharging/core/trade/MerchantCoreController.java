package com.example.evcharging.core.trade;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.DataScopeType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/merchant-api/v1/core")
public class MerchantCoreController {
    private final JdbcTemplate jdbc;
    public MerchantCoreController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/dashboard")
    public ApiResponse<Dashboard> dashboard(){
        var p=RequestContext.requirePrincipal();
        SqlScope scope=scope(p);
        int charging=jdbc.queryForObject("SELECT COUNT(*) FROM charging_session WHERE tenant_id=? AND status=30"+scope.sql(),
                Integer.class,args(p,scope));
        long todayEnergy=jdbc.queryForObject("SELECT COALESCE(SUM(energy_wh),0) FROM charge_order WHERE tenant_id=? AND create_time>=CURDATE()"+scope.sql(),
                Long.class,args(p,scope));
        long todayRevenue=jdbc.queryForObject("SELECT COALESCE(SUM(receivable_amount_fen),0) FROM charge_order WHERE tenant_id=? AND create_time>=CURDATE()"+scope.sql(),
                Long.class,args(p,scope));
        return ApiResponse.success(new Dashboard(charging,todayEnergy,todayRevenue));
    }

    @GetMapping("/orders")
    public ApiResponse<List<OrderView>> orders(@RequestParam(defaultValue="100") int limit){
        var p=RequestContext.requirePrincipal();SqlScope scope=scope(p);int size=Math.max(1,Math.min(limit,200));
        List<Object> args=new ArrayList<>();args.add(p.tenantId());args.addAll(scope.stationArgs());args.add(size);
        return ApiResponse.success(jdbc.query("""
            SELECT order_no,session_no,station_id,energy_wh,receivable_amount_fen,paid_amount_fen,create_time
            FROM charge_order WHERE tenant_id=?
            """+scope.sql()+" ORDER BY id DESC LIMIT ?",(rs,n)->new OrderView(rs.getString(1),rs.getString(2),rs.getLong(3),
            rs.getLong(4),rs.getLong(5),rs.getLong(6),String.valueOf(rs.getObject(7))),args.toArray()));
    }

    private SqlScope scope(com.example.evcharging.framework.security.AccessPrincipal p){
        if(p.dataScopeType()==DataScopeType.ALL||p.dataScopeType()==DataScopeType.TENANT) return new SqlScope("",List.of());
        if(p.dataScopeType()==DataScopeType.STATION&&!p.stationIds().isEmpty()){
            String placeholders=String.join(",",Collections.nCopies(p.stationIds().size(),"?"));
            return new SqlScope(" AND station_id IN ("+placeholders+")",new ArrayList<>(p.stationIds()));
        }
        return new SqlScope(" AND 1=0",List.of());
    }
    private Object[] args(com.example.evcharging.framework.security.AccessPrincipal p,SqlScope s){
        List<Object> a=new ArrayList<>();a.add(p.tenantId());a.addAll(s.stationArgs());return a.toArray();
    }

    private record SqlScope(String sql,List<?> stationArgs){}
    public record Dashboard(int activeCharging,long todayEnergyWh,long todayRevenueFen){}
    public record OrderView(String orderNo,String sessionNo,long stationId,long energyWh,long receivableAmountFen,long paidAmountFen,String createTime){}
}
