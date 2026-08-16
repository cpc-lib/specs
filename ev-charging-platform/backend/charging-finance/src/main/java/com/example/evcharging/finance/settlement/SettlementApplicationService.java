package com.example.evcharging.finance.settlement;

import com.example.evcharging.finance.ledger.LedgerPosting;
import com.example.evcharging.finance.ledger.LedgerPostingService;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SettlementApplicationService {
    private static final int MAX_SOURCES_PER_BATCH = 500;
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final LedgerPostingService ledger;

    public SettlementApplicationService(JdbcTemplate jdbc,IdGenerator ids,LedgerPostingService ledger){
        this.jdbc=jdbc;this.ids=ids;this.ledger=ledger;
    }

    public record RuleItemRequest(String participantType,String participantId,int ratioBps){}
    public record CreateRuleRequest(String ruleCode,String ruleName,int versionNo,LocalDateTime effectiveFrom,List<RuleItemRequest> items){}
    public record RunRequest(String requestId,LocalDate businessDate,long ruleVersionId){}
    public record RunResult(String batchNo,int sourceCount,long settlementAmountFen){}
    private record Source(long id,String paymentNo,long base){}
    private record BatchRow(long id,String status,long createdBy){}
    private record OrderRow(long id,String orderNo,long sourceId,String paymentNo,long base){}
    private record DetailRow(String participantType,String participantId,long amount){}

    @Transactional
    public long createPublishedRule(CreateRuleRequest request){
        long tenant=RequestContext.requireTenantId();
        String code=required(request.ruleCode(),"ruleCode"),name=required(request.ruleName(),"ruleName");
        if(request.versionNo()<=0)throw new IllegalArgumentException("versionNo must be positive");
        List<SettlementCalculator.RuleItem> items=(request.items()==null?List.<RuleItemRequest>of():request.items()).stream()
                .map(x->new SettlementCalculator.RuleItem(x.participantType(),x.participantId(),x.ratioBps())).toList();
        SettlementCalculator.calculate(10000,items);
        List<Long> existing=jdbc.query("SELECT id FROM finance_settlement_rule WHERE tenant_id=? AND rule_code=?",(rs,n)->rs.getLong(1),tenant,code);
        long ruleId;
        LocalDateTime now=LocalDateTime.now();
        if(existing.isEmpty()){
            ruleId=ids.nextId();
            jdbc.update("INSERT INTO finance_settlement_rule(id,tenant_id,rule_code,rule_name,status,create_time,update_time) VALUES (?,?,?,?,'ACTIVE',?,?)",
                    ruleId,tenant,code,name,now,now);
        }else ruleId=existing.get(0);
        List<Long> prior=jdbc.query("SELECT id FROM finance_settlement_rule_version WHERE rule_id=? AND version_no=?",(rs,n)->rs.getLong(1),ruleId,request.versionNo());
        if(!prior.isEmpty())return prior.get(0);
        long versionId=ids.nextId(); LocalDateTime effective=request.effectiveFrom()==null?now:request.effectiveFrom();
        jdbc.update("""
            INSERT INTO finance_settlement_rule_version(
              id,tenant_id,rule_id,version_no,status,effective_from,published_time,create_time
            ) VALUES (?,?,?,?,'PUBLISHED',?,?,?)
            """,versionId,tenant,ruleId,request.versionNo(),effective,now,now);
        int priority=0;
        for(var item:items) jdbc.update("""
            INSERT INTO finance_settlement_rule_item(
              id,tenant_id,version_id,participant_type,participant_id,calculation_type,ratio_bps,priority_no,create_time
            ) VALUES (?,?,?,?,?,'RATIO_BPS',?,?,?)
            """,ids.nextId(),tenant,versionId,item.participantType(),item.participantId(),item.ratioBps(),priority++,now);
        return versionId;
    }

    @Transactional
    public RunResult run(RunRequest request){
        long tenant=RequestContext.requireTenantId();
        long creator=RequestContext.currentUserId().orElse(0L);
        String requestId=required(request.requestId(),"requestId");
        LocalDate date=Objects.requireNonNull(request.businessDate(),"businessDate required");
        List<RunResult> prior=jdbc.query("SELECT batch_no,source_count,settlement_amount_fen FROM finance_settlement_batch WHERE tenant_id=? AND request_id=?",
                (rs,n)->new RunResult(rs.getString(1),rs.getInt(2),rs.getLong(3)),tenant,requestId);
        if(!prior.isEmpty())return prior.get(0);

        List<SettlementCalculator.RuleItem> items=jdbc.query("""
            SELECT i.participant_type,i.participant_id,i.ratio_bps
            FROM finance_settlement_rule_item i
            JOIN finance_settlement_rule_version v ON v.id=i.version_id
            WHERE i.tenant_id=? AND i.version_id=? AND i.calculation_type='RATIO_BPS'
              AND v.status='PUBLISHED' AND v.effective_from<? AND (v.effective_to IS NULL OR v.effective_to>=?)
            ORDER BY i.priority_no,i.id
            """,(rs,n)->new SettlementCalculator.RuleItem(rs.getString(1),rs.getString(2),rs.getInt(3)),
                tenant,request.ruleVersionId(),date.plusDays(1).atStartOfDay(),date.atStartOfDay());
        if(items.isEmpty())throw new IllegalArgumentException("published settlement rule version not found");

        long batchId=ids.nextId(); String batchNo="STB"+batchId; LocalDateTime now=LocalDateTime.now();
        try {
            jdbc.update("""
                INSERT INTO finance_settlement_batch(
                  id,tenant_id,batch_no,request_id,business_date,rule_version_id,status,created_by,started_time,create_time
                ) VALUES (?,?,?,?,?,?,'CALCULATING',?,?,?)
                """,batchId,tenant,batchNo,requestId,date,request.ruleVersionId(),creator,now,now);
        } catch (DuplicateKeyException race) {
            List<RunResult> raced=jdbc.query("SELECT batch_no,source_count,settlement_amount_fen FROM finance_settlement_batch WHERE tenant_id=? AND request_id=?",
                    (rs,n)->new RunResult(rs.getString(1),rs.getInt(2),rs.getLong(3)),tenant,requestId);
            if(!raced.isEmpty()) return raced.get(0);
            throw race;
        }

        List<Source> sources=jdbc.query("""
            SELECT id,payment_no,settlement_base_amount_fen
            FROM finance_settlement_source
            WHERE tenant_id=? AND business_date=? AND status='READY'
            ORDER BY id LIMIT ? FOR UPDATE
            """,(rs,n)->new Source(rs.getLong(1),rs.getString(2),rs.getLong(3)),tenant,date,MAX_SOURCES_PER_BATCH);

        long total=0;
        for(Source source:sources){
            List<SettlementCalculator.Allocation> allocations=SettlementCalculator.calculate(source.base(),items);
            long allocated=allocations.stream().mapToLong(SettlementCalculator.Allocation::amountFen).sum();
            if(allocated!=source.base())throw new IllegalStateException("settlement allocation not balanced");
            long orderId=ids.nextId();
            jdbc.update("""
                INSERT INTO finance_settlement_order(
                  id,tenant_id,batch_id,settlement_order_no,source_id,payment_no,
                  settlement_base_amount_fen,allocated_amount_fen,currency,status,create_time
                ) VALUES (?,?,?,?,?,?,?,?,?,'CALCULATED',?)
                """,orderId,tenant,batchId,"STO"+orderId,source.id(),source.paymentNo(),source.base(),allocated,"CNY",now);
            for(var allocation:allocations) jdbc.update("""
                INSERT INTO finance_settlement_detail(
                  id,tenant_id,settlement_order_id,participant_type,participant_id,amount_fen,currency,create_time
                ) VALUES (?,?,?,?,?,?,?,?)
                """,ids.nextId(),tenant,orderId,allocation.participantType(),allocation.participantId(),allocation.amountFen(),"CNY",now);
            int claimed=jdbc.update("UPDATE finance_settlement_source SET status='ALLOCATED',update_time=? WHERE id=? AND status='READY'",now,source.id());
            if(claimed!=1)throw new IllegalStateException("settlement source claim lost: "+source.id());
            total=Math.addExact(total,allocated);
        }

        String status=sources.isEmpty()?"COMPLETED":"PENDING_APPROVAL";
        jdbc.update("""
            UPDATE finance_settlement_batch
            SET status=?,source_count=?,settlement_amount_fen=?,completed_time=CASE WHEN ?='COMPLETED' THEN NOW(3) ELSE NULL END
            WHERE id=?
            """,status,sources.size(),total,status,batchId);
        return new RunResult(batchNo,sources.size(),total);
    }

    @Transactional
    public void approve(String batchNo,String comment){
        long tenant=RequestContext.requireTenantId(); long approver=RequestContext.requireUserId();
        BatchRow batch=lockBatch(tenant,batchNo);
        if("COMPLETED".equals(batch.status()))return;
        if(!"PENDING_APPROVAL".equals(batch.status()))throw new IllegalStateException("batch is not pending approval");
        if(batch.createdBy()>0&&batch.createdBy()==approver)throw new IllegalStateException("maker-checker violation: creator cannot approve");
        LocalDateTime now=LocalDateTime.now();
        List<OrderRow> orders=jdbc.query("""
            SELECT id,settlement_order_no,source_id,payment_no,settlement_base_amount_fen
            FROM finance_settlement_order WHERE tenant_id=? AND batch_id=? ORDER BY id FOR UPDATE
            """,(rs,n)->new OrderRow(rs.getLong(1),rs.getString(2),rs.getLong(3),rs.getString(4),rs.getLong(5)),tenant,batch.id());
        for(OrderRow order:orders){
            List<DetailRow> details=jdbc.query("SELECT participant_type,participant_id,amount_fen FROM finance_settlement_detail WHERE tenant_id=? AND settlement_order_id=? ORDER BY id",
                    (rs,n)->new DetailRow(rs.getString(1),rs.getString(2),rs.getLong(3)),tenant,order.id());
            List<LedgerPosting.Entry> entries=new ArrayList<>();
            entries.add(new LedgerPosting.Entry("CHARGING_RECEIVABLE_CLEARING","PAYMENT",order.paymentNo(),LedgerPosting.Side.DEBIT,order.base()));
            for(DetailRow detail:details){
                String account="PLATFORM".equalsIgnoreCase(detail.participantType())?"PLATFORM_REVENUE":"SETTLEMENT_PAYABLE_"+detail.participantType().toUpperCase();
                entries.add(new LedgerPosting.Entry(account,detail.participantType(),detail.participantId(),LedgerPosting.Side.CREDIT,detail.amount()));
            }
            ledger.post(tenant,"SETTLEMENT:"+order.orderNo(),"finance.settlement.approved",order.orderNo(),now,new LedgerPosting(entries));
            jdbc.update("UPDATE finance_settlement_order SET status='APPROVED' WHERE id=? AND status='CALCULATED'",order.id());
            int settled=jdbc.update("UPDATE finance_settlement_source SET status='SETTLED',update_time=? WHERE id=? AND status='ALLOCATED'",now,order.sourceId());
            if(settled!=1)throw new IllegalStateException("settlement source approval state changed: "+order.sourceId());
        }
        jdbc.update("""
            UPDATE finance_settlement_batch
            SET status='COMPLETED',approved_by=?,approved_time=?,approval_comment=?,completed_time=?
            WHERE id=? AND status='PENDING_APPROVAL'
            """,approver,now,comment,now,batch.id());
    }

    @Transactional
    public void reject(String batchNo,String comment){
        long tenant=RequestContext.requireTenantId();long rejector=RequestContext.requireUserId();
        BatchRow batch=lockBatch(tenant,batchNo);
        if("REJECTED".equals(batch.status()))return;
        if(!"PENDING_APPROVAL".equals(batch.status()))throw new IllegalStateException("batch is not pending approval");
        if(batch.createdBy()>0&&batch.createdBy()==rejector)throw new IllegalStateException("maker-checker violation: creator cannot reject own batch");
        LocalDateTime now=LocalDateTime.now();
        List<Long> sources=jdbc.query("SELECT source_id FROM finance_settlement_order WHERE tenant_id=? AND batch_id=? FOR UPDATE",(rs,n)->rs.getLong(1),tenant,batch.id());
        for(Long sourceId:sources)jdbc.update("UPDATE finance_settlement_source SET status='READY',update_time=? WHERE id=? AND status='ALLOCATED'",now,sourceId);
        jdbc.update("UPDATE finance_settlement_order SET status='REJECTED' WHERE tenant_id=? AND batch_id=? AND status='CALCULATED'",tenant,batch.id());
        jdbc.update("""
            UPDATE finance_settlement_batch
            SET status='REJECTED',rejected_by=?,rejected_time=?,approval_comment=?,completed_time=?
            WHERE id=? AND status='PENDING_APPROVAL'
            """,rejector,now,comment,now,batch.id());
    }

    private BatchRow lockBatch(long tenant,String batchNo){
        List<BatchRow> rows=jdbc.query("SELECT id,status,created_by FROM finance_settlement_batch WHERE tenant_id=? AND batch_no=? FOR UPDATE",
                (rs,n)->new BatchRow(rs.getLong(1),rs.getString(2),rs.getLong(3)),tenant,batchNo);
        if(rows.isEmpty())throw new IllegalArgumentException("settlement batch not found");return rows.get(0);
    }
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" required");return v.trim();}
}
