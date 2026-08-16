package com.example.evcharging.operation.inspection;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/operation/inspections")
public class InspectionAdminController {
    private final InspectionPlanService plans;private final InspectionTaskService tasks;private final JdbcTemplate jdbc;
    public InspectionAdminController(InspectionPlanService plans,InspectionTaskService tasks,JdbcTemplate jdbc){
        this.plans=plans;this.tasks=tasks;this.jdbc=jdbc;
    }

    @PostMapping("/plans")
    public ApiResponse<Map<String,Object>> createPlan(@RequestBody InspectionPlanService.CreatePlanCommand c){
        return ApiResponse.success(plans.create(c));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<TaskView>> list(@RequestParam(defaultValue="100") int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        return ApiResponse.success(jdbc.query("""
            SELECT task_no,plan_id,station_id,scheduled_date,status,assignee_user_id,overdue,
                   started_time,completed_time
            FROM operation_inspection_task WHERE tenant_id=? ORDER BY scheduled_date DESC,id DESC LIMIT ?
            """,(rs,n)->new TaskView(rs.getString(1),rs.getLong(2),rs.getLong(3),String.valueOf(rs.getObject(4)),
                rs.getString(5),(Long)rs.getObject(6),rs.getBoolean(7),String.valueOf(rs.getObject(8)),
                String.valueOf(rs.getObject(9))),tenant,size));
    }

    @PostMapping("/tasks/{taskNo}/start")
    public ApiResponse<Map<String,String>> start(@PathVariable String taskNo){
        tasks.start(taskNo);return ApiResponse.success(Map.of("result","IN_PROGRESS"));
    }

    @PostMapping("/tasks/{taskNo}/complete")
    public ApiResponse<Map<String,String>> complete(@PathVariable String taskNo,@RequestBody JsonNode result){
        tasks.complete(taskNo,result);return ApiResponse.success(Map.of("result","COMPLETED"));
    }

    public record TaskView(String taskNo,long planId,long stationId,String scheduledDate,String status,
                           Long assigneeUserId,boolean overdue,String startedTime,String completedTime){}
}
