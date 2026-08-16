package com.example.evcharging.operation;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.DataScopeType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/merchant-api/v1/operation")
public class MerchantOperationController {
    private final JdbcTemplate jdbc;
    public MerchantOperationController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/alarms")
    public ApiResponse<List<AlarmView>> alarms(){
        var p=RequestContext.requirePrincipal();
        StringBuilder sql=new StringBuilder("""
            SELECT alarm_no,station_id,device_id,alarm_code,severity,status,last_occurred_time
            FROM operation_alarm WHERE tenant_id=?
            """);
        List<Object> args=new ArrayList<>();args.add(p.tenantId());scope(sql,args,"station_id",p.dataScopeType(),p.stationIds());
        sql.append(" ORDER BY id DESC LIMIT 100");
        return ApiResponse.success(jdbc.query(sql.toString(),(rs,n)->new AlarmView(rs.getString(1),(Long)rs.getObject(2),
                rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),String.valueOf(rs.getObject(7))),args.toArray()));
    }

    @GetMapping("/work-orders")
    public ApiResponse<List<WorkOrderView>> workOrders(){
        var p=RequestContext.requirePrincipal();
        StringBuilder sql=new StringBuilder("""
            SELECT work_order_no,station_id,title,priority,status,assignee_user_id,sla_status,create_time
            FROM operation_work_order WHERE tenant_id=?
            """);
        List<Object> args=new ArrayList<>();args.add(p.tenantId());scope(sql,args,"station_id",p.dataScopeType(),p.stationIds());
        sql.append(" ORDER BY id DESC LIMIT 100");
        return ApiResponse.success(jdbc.query(sql.toString(),(rs,n)->new WorkOrderView(rs.getString(1),(Long)rs.getObject(2),
                rs.getString(3),rs.getString(4),rs.getString(5),(Long)rs.getObject(6),rs.getString(7),
                String.valueOf(rs.getObject(8))),args.toArray()));
    }

    static void scope(StringBuilder sql,List<Object> args,String column,DataScopeType scope,Set<Long> stationIds){
        if(scope==DataScopeType.ALL||scope==DataScopeType.TENANT)return;
        if(scope==DataScopeType.STATION&&!stationIds.isEmpty()){
            sql.append(" AND ").append(column).append(" IN (")
                    .append(String.join(",",Collections.nCopies(stationIds.size(),"?"))).append(")");
            args.addAll(stationIds);return;
        }
        sql.append(" AND 1=0");
    }

    public record AlarmView(String alarmNo,Long stationId,String deviceId,String alarmCode,String severity,String status,String lastOccurredTime){}
    public record WorkOrderView(String workOrderNo,Long stationId,String title,String priority,String status,
                                Long assigneeUserId,String slaStatus,String createTime){}
}
