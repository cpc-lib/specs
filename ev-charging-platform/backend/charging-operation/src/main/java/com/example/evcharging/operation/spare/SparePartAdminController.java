package com.example.evcharging.operation.spare;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/operation/spare-parts")
public class SparePartAdminController {
    private final SpareStockService service;private final JdbcTemplate jdbc;
    public SparePartAdminController(SpareStockService service,JdbcTemplate jdbc){this.service=service;this.jdbc=jdbc;}

    @PostMapping
    public ApiResponse<Map<String,Long>> create(@RequestBody SpareStockService.CreatePartCommand c){
        return ApiResponse.success(Map.of("partId",service.createPart(c)));
    }
    @PostMapping("/receive")
    public ApiResponse<SpareStockService.MovementResult> receive(@RequestBody SpareStockService.MovementCommand c){
        return ApiResponse.success(service.receive(c));
    }
    @PostMapping("/consume")
    public ApiResponse<SpareStockService.MovementResult> consume(@RequestBody SpareStockService.MovementCommand c){
        return ApiResponse.success(service.consume(c));
    }

    @GetMapping("/stock")
    public ApiResponse<List<StockView>> stock(){
        long tenant=RequestContext.requireTenantId();
        return ApiResponse.success(jdbc.query("""
            SELECT p.part_code,p.part_name,p.unit,p.min_stock_qty,s.warehouse_code,s.available_qty,
                   CASE WHEN s.available_qty<p.min_stock_qty THEN 1 ELSE 0 END
            FROM operation_spare_stock s
            JOIN operation_spare_part p ON p.id=s.part_id
            WHERE s.tenant_id=? ORDER BY s.warehouse_code,p.part_code
            """,(rs,n)->new StockView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4),
                rs.getString(5),rs.getInt(6),rs.getBoolean(7)),tenant));
    }

    @GetMapping("/transactions")
    public ApiResponse<List<TxView>> transactions(@RequestParam(defaultValue="100") int limit){
        long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,500));
        return ApiResponse.success(jdbc.query("""
            SELECT t.transaction_no,t.request_id,t.warehouse_code,p.part_code,t.change_type,t.quantity_delta,
                   t.balance_after,t.work_order_no,t.reference_no,t.operator_user_id,t.create_time
            FROM operation_spare_stock_transaction t
            JOIN operation_spare_part p ON p.id=t.part_id
            WHERE t.tenant_id=? ORDER BY t.id DESC LIMIT ?
            """,(rs,n)->new TxView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getInt(6),rs.getInt(7),rs.getString(8),rs.getString(9),
                (Long)rs.getObject(10),String.valueOf(rs.getObject(11))),tenant,size));
    }

    public record StockView(String partCode,String partName,String unit,int minStockQty,String warehouseCode,int availableQty,boolean lowStock){}
    public record TxView(String transactionNo,String requestId,String warehouseCode,String partCode,String changeType,
                         int quantityDelta,int balanceAfter,String workOrderNo,String referenceNo,Long operatorUserId,String createTime){}
}
