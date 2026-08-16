package com.example.evcharging.finance.settlement;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/v1/finance/settlement")
public class SettlementAdminController {
    private final SettlementApplicationService service;private final JdbcTemplate jdbc;
    public SettlementAdminController(SettlementApplicationService service,JdbcTemplate jdbc){this.service=service;this.jdbc=jdbc;}

    @PostMapping("/rules") public ApiResponse<Map<String,Long>> createRule(@RequestBody SettlementApplicationService.CreateRuleRequest request){return ApiResponse.ok(Map.of("ruleVersionId",service.createPublishedRule(request)));}
    @GetMapping("/rules") public ApiResponse<List<RuleView>> rules(){long t=RequestContext.requireTenantId();return ApiResponse.ok(jdbc.query("SELECT r.rule_code,r.rule_name,v.id,v.version_no,v.status,v.effective_from FROM finance_settlement_rule r JOIN finance_settlement_rule_version v ON v.rule_id=r.id WHERE r.tenant_id=? ORDER BY v.id DESC",(rs,n)->new RuleView(rs.getString(1),rs.getString(2),rs.getLong(3),rs.getInt(4),rs.getString(5),String.valueOf(rs.getObject(6))),t));}
    @PostMapping("/run") public ApiResponse<SettlementApplicationService.RunResult> run(@RequestBody SettlementApplicationService.RunRequest request){return ApiResponse.ok(service.run(request));}
    public record DecisionRequest(String comment){}
    @PostMapping("/{batchNo}/approve") public ApiResponse<Map<String,String>> approve(@PathVariable String batchNo,@RequestBody(required=false) DecisionRequest request){service.approve(batchNo,request==null?null:request.comment());return ApiResponse.ok(Map.of("status","COMPLETED"));}
    @PostMapping("/{batchNo}/reject") public ApiResponse<Map<String,String>> reject(@PathVariable String batchNo,@RequestBody(required=false) DecisionRequest request){service.reject(batchNo,request==null?null:request.comment());return ApiResponse.ok(Map.of("status","REJECTED"));}
    @GetMapping("/batches") public ApiResponse<List<BatchView>> batches(@RequestParam(defaultValue="50")int limit){long t=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,200));return ApiResponse.ok(jdbc.query("SELECT batch_no,business_date,rule_version_id,status,source_count,settlement_amount_fen,created_by,approved_by,completed_time FROM finance_settlement_batch WHERE tenant_id=? ORDER BY id DESC LIMIT ?",(rs,n)->new BatchView(rs.getString(1),String.valueOf(rs.getObject(2)),rs.getLong(3),rs.getString(4),rs.getInt(5),rs.getLong(6),rs.getLong(7),(Long)rs.getObject(8),String.valueOf(rs.getObject(9))),t,size));}
    @GetMapping("/{batchNo}/details") public ApiResponse<List<DetailView>> details(@PathVariable String batchNo){long t=RequestContext.requireTenantId();return ApiResponse.ok(jdbc.query("SELECT o.settlement_order_no,o.payment_no,o.settlement_base_amount_fen,d.participant_type,d.participant_id,d.amount_fen FROM finance_settlement_batch b JOIN finance_settlement_order o ON o.batch_id=b.id JOIN finance_settlement_detail d ON d.settlement_order_id=o.id WHERE b.tenant_id=? AND b.batch_no=? ORDER BY o.id,d.id",(rs,n)->new DetailView(rs.getString(1),rs.getString(2),rs.getLong(3),rs.getString(4),rs.getString(5),rs.getLong(6)),t,batchNo));}
    public record RuleView(String ruleCode,String ruleName,long versionId,int versionNo,String status,String effectiveFrom){}
    public record BatchView(String batchNo,String businessDate,long ruleVersionId,String status,int sourceCount,long amountFen,long createdBy,Long approvedBy,String completedTime){}
    public record DetailView(String settlementOrderNo,String paymentNo,long baseAmountFen,String participantType,String participantId,long amountFen){}
}
