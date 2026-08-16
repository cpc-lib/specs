package com.example.evcharging.operation.technician;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.operation.inspection.InspectionTaskService;
import com.example.evcharging.operation.workorder.WorkOrderWorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/technician-api/v1/operation")
public class TechnicianOperationController {
    private final JdbcTemplate jdbc;
    private final WorkOrderWorkflowService workOrders;
    private final InspectionTaskService inspections;

    public TechnicianOperationController(JdbcTemplate jdbc,WorkOrderWorkflowService workOrders,InspectionTaskService inspections){
        this.jdbc=jdbc;this.workOrders=workOrders;this.inspections=inspections;
    }

    @GetMapping("/work-orders")
    public ApiResponse<List<WorkOrderView>> workOrders(){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        return ApiResponse.success(jdbc.query("""
            SELECT work_order_no,alarm_no,title,priority,status,response_due_time,resolution_due_time,sla_status
            FROM operation_work_order
            WHERE tenant_id=? AND assignee_user_id=?
            ORDER BY CASE WHEN status IN ('CLOSED','CANCELLED') THEN 1 ELSE 0 END, resolution_due_time,id DESC
            LIMIT 200
            """,(rs,n)->new WorkOrderView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),String.valueOf(rs.getObject(6)),String.valueOf(rs.getObject(7)),rs.getString(8)),
                tenant,user));
    }

    @PostMapping("/work-orders/{no}/start")
    public ApiResponse<Map<String,String>> startWorkOrder(@PathVariable String no){
        workOrders.startRepair(no);return ApiResponse.success(Map.of("result","IN_PROGRESS"));
    }

    @PostMapping("/work-orders/{no}/repair")
    public ApiResponse<Map<String,String>> repair(@PathVariable String no,@RequestBody RepairCommand c){
        workOrders.completeRepair(no,c.summary());return ApiResponse.success(Map.of("result","WAIT_VERIFY"));
    }

    @GetMapping("/inspections")
    public ApiResponse<List<InspectionView>> inspections(){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        return ApiResponse.success(jdbc.query("""
            SELECT task_no,station_id,scheduled_date,status,overdue,checklist_json,result_json
            FROM operation_inspection_task
            WHERE tenant_id=? AND assignee_user_id=?
            ORDER BY CASE WHEN status='COMPLETED' THEN 1 ELSE 0 END,scheduled_date,id DESC
            LIMIT 200
            """,(rs,n)->new InspectionView(rs.getString(1),rs.getLong(2),String.valueOf(rs.getObject(3)),
                rs.getString(4),rs.getBoolean(5),rs.getString(6),rs.getString(7)),tenant,user));
    }

    @PostMapping("/inspections/{taskNo}/start")
    public ApiResponse<Map<String,String>> startInspection(@PathVariable String taskNo){
        inspections.start(taskNo);return ApiResponse.success(Map.of("result","IN_PROGRESS"));
    }

    @PostMapping("/inspections/{taskNo}/complete")
    public ApiResponse<Map<String,String>> completeInspection(@PathVariable String taskNo,@RequestBody JsonNode result){
        inspections.complete(taskNo,result);return ApiResponse.success(Map.of("result","COMPLETED"));
    }

    @GetMapping("/spare-stock")
    public ApiResponse<List<StockView>> spareStock(){
        long tenant=RequestContext.requireTenantId();
        return ApiResponse.success(jdbc.query("""
            SELECT p.part_code,p.part_name,p.unit,s.warehouse_code,s.available_qty,p.min_stock_qty
            FROM operation_spare_stock s
            JOIN operation_spare_part p ON p.id=s.part_id
            WHERE s.tenant_id=? ORDER BY s.warehouse_code,p.part_code
            """,(rs,n)->new StockView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getInt(5),rs.getInt(6)),tenant));
    }

    public record RepairCommand(String summary){}
    public record WorkOrderView(String workOrderNo,String alarmNo,String title,String priority,String status,
                                String responseDueTime,String resolutionDueTime,String slaStatus){}
    public record InspectionView(String taskNo,long stationId,String scheduledDate,String status,boolean overdue,
                                 String checklistJson,String resultJson){}
    public record StockView(String partCode,String partName,String unit,String warehouseCode,int availableQty,int minStockQty){}
}
