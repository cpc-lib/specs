package com.example.evcharging.operation.workorder;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/operation/work-orders")
public class WorkOrderAdminController {
    private final JdbcTemplate jdbc;
    private final WorkOrderWorkflowService workflow;
    public WorkOrderAdminController(JdbcTemplate jdbc,WorkOrderWorkflowService workflow){this.jdbc=jdbc;this.workflow=workflow;}

    @GetMapping
    public ApiResponse<List<WorkOrderView>> list(@RequestParam(required=false) String status,@RequestParam(defaultValue="100") int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        String base="""
            SELECT work_order_no,alarm_no,title,priority,status,assignee_user_id,dispatcher_user_id,verifier_user_id,
                   response_due_time,resolution_due_time,first_response_time,repair_started_time,repair_completed_time,
                   resolved_time,sla_status,create_time
            FROM operation_work_order WHERE tenant_id=?
            """;
        List<WorkOrderView> rows;
        if(status==null||status.isBlank()){
            rows=jdbc.query(base+" ORDER BY id DESC LIMIT ?",(rs,n)->map(rs),tenant,size);
        }else{
            rows=jdbc.query(base+" AND status=? ORDER BY id DESC LIMIT ?",(rs,n)->map(rs),tenant,status.toUpperCase(),size);
        }
        return ApiResponse.success(rows);
    }

    @PostMapping("/{no}/assign")
    public ApiResponse<Map<String,String>> assign(@PathVariable String no,@RequestBody AssignRequest request){
        workflow.assign(no,request.assigneeUserId());return ApiResponse.success(Map.of("result","ASSIGNED"));
    }
    @PostMapping("/{no}/start")
    public ApiResponse<Map<String,String>> start(@PathVariable String no){
        workflow.startRepair(no);return ApiResponse.success(Map.of("result","IN_PROGRESS"));
    }
    @PostMapping("/{no}/repair")
    public ApiResponse<Map<String,String>> repair(@PathVariable String no,@RequestBody RepairRequest request){
        workflow.completeRepair(no,request.summary());return ApiResponse.success(Map.of("result","WAIT_VERIFY"));
    }
    @PostMapping("/{no}/verify")
    public ApiResponse<Map<String,String>> verify(@PathVariable String no,@RequestBody VerifyRequest request){
        workflow.verify(no,request.passed(),request.comment());
        return ApiResponse.success(Map.of("result",request.passed()?"CLOSED":"IN_PROGRESS"));
    }

    private WorkOrderView map(java.sql.ResultSet rs)throws java.sql.SQLException{
        return new WorkOrderView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                (Long)rs.getObject(6),(Long)rs.getObject(7),(Long)rs.getObject(8),String.valueOf(rs.getObject(9)),
                String.valueOf(rs.getObject(10)),String.valueOf(rs.getObject(11)),String.valueOf(rs.getObject(12)),
                String.valueOf(rs.getObject(13)),String.valueOf(rs.getObject(14)),rs.getString(15),String.valueOf(rs.getObject(16)));
    }

    public record AssignRequest(long assigneeUserId){}
    public record RepairRequest(String summary){}
    public record VerifyRequest(boolean passed,String comment){}
    public record WorkOrderView(String workOrderNo,String alarmNo,String title,String priority,String status,
                                Long assigneeUserId,Long dispatcherUserId,Long verifierUserId,String responseDueTime,
                                String resolutionDueTime,String firstResponseTime,String repairStartedTime,String repairCompletedTime,
                                String resolvedTime,String slaStatus,String createTime){}
}
