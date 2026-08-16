package com.example.evcharging.finance.reconciliation;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReconciliationApplicationService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public ReconciliationApplicationService(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    public record RunRequest(String requestId, String channel, String merchantId, LocalDate businessDate) {}
    public record RunResult(String batchNo, int localCount, int channelCount, int matchCount, int differenceCount) {}
    private record LocalRow(long id,String paymentNo,String orderNo,long stationId,String trade,long originalAmount,long paymentAdjustment,
                            long effectiveAmount,long originalRefund,long refundAdjustment,long effectiveRefund,
                            String status,String channel,String merchant) {}
    private record ChannelRow(long id,String paymentNo,String trade,long amount,long refund,String status) {}
    private record ExistingSource(long id,String status,long gross,long refund,long base) {}

    @Transactional
    public RunResult run(RunRequest request) {
        long tenant = RequestContext.requireTenantId();
        String requestId = required(request.requestId(), "requestId");
        String channel = required(request.channel(), "channel").toUpperCase();
        String merchant = request.merchantId() == null || request.merchantId().isBlank() ? "DEFAULT" : request.merchantId().trim();
        LocalDate businessDate = Objects.requireNonNull(request.businessDate(), "businessDate required");

        List<RunResult> prior = jdbc.query("""
            SELECT batch_no,local_count,channel_count,match_count,difference_count
            FROM finance_reconciliation_batch WHERE tenant_id=? AND request_id=?
            """, (rs,n) -> new RunResult(rs.getString(1),rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getInt(5)),
                tenant,requestId);
        if (!prior.isEmpty()) return prior.get(0);

        long batchId = ids.nextId();
        String batchNo = "RC" + batchId;
        LocalDateTime now = LocalDateTime.now();
        try {
            jdbc.update("""
                INSERT INTO finance_reconciliation_batch(
                  id,tenant_id,batch_no,request_id,channel,merchant_id,business_date,status,started_time,create_time
                ) VALUES (?,?,?,?,?,?,?,'RUNNING',?,?)
                """, batchId,tenant,batchNo,requestId,channel,merchant,businessDate,now,now);
        } catch (DuplicateKeyException race) {
            List<RunResult> raced = jdbc.query("SELECT batch_no,local_count,channel_count,match_count,difference_count FROM finance_reconciliation_batch WHERE tenant_id=? AND request_id=?",
                    (rs,n)->new RunResult(rs.getString(1),rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getInt(5)),tenant,requestId);
            if(!raced.isEmpty()) return raced.get(0);
            throw race;
        }

        List<LocalRow> locals = jdbc.query("""
            SELECT p.id,p.payment_no,p.biz_order_no,p.station_id,p.channel_trade_no,p.amount_fen,
                   COALESCE((SELECT SUM(a.amount_fen) FROM finance_adjustment_order a
                             WHERE a.tenant_id=p.tenant_id AND a.payment_no=p.payment_no
                               AND a.adjustment_type='PAYMENT_AMOUNT' AND a.status='POSTED'),0) AS payment_adjustment,
                   COALESCE((SELECT SUM(r.amount_fen) FROM finance_refund_fact r
                             WHERE r.tenant_id=p.tenant_id AND r.payment_no=p.payment_no AND r.refund_status='SUCCESS'),0) AS original_refund,
                   COALESCE((SELECT SUM(a.amount_fen) FROM finance_adjustment_order a
                             WHERE a.tenant_id=p.tenant_id AND a.payment_no=p.payment_no
                               AND a.adjustment_type='REFUND_AMOUNT' AND a.status='POSTED'),0) AS refund_adjustment,
                   p.payment_status,p.channel,p.merchant_id
            FROM finance_transaction_fact p
            WHERE p.tenant_id=? AND p.channel=? AND p.merchant_id=? AND p.business_date=?
            ORDER BY p.id
            """, (rs,n) -> {
                    long originalAmount=rs.getLong(6), paymentAdj=rs.getLong(7), originalRefund=rs.getLong(8), refundAdj=rs.getLong(9);
                    return new LocalRow(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getLong(4),rs.getString(5),
                            originalAmount,paymentAdj,Math.addExact(originalAmount,paymentAdj),
                            originalRefund,refundAdj,Math.addExact(originalRefund,refundAdj),
                            rs.getString(10),rs.getString(11),rs.getString(12));
                }, tenant,channel,merchant,Date.valueOf(businessDate));

        for (LocalRow local : locals) {
            if (local.effectiveAmount() < 0 || local.effectiveRefund() < 0 || local.effectiveRefund() > local.effectiveAmount()) {
                throw new IllegalStateException("invalid adjusted local fact for payment " + local.paymentNo());
            }
        }

        List<ChannelRow> channels = jdbc.query("""
            SELECT id,payment_no,channel_trade_no,amount_fen,refund_amount_fen,channel_status
            FROM finance_channel_transaction
            WHERE tenant_id=? AND channel=? AND merchant_id=? AND business_date=?
            ORDER BY id
            """, (rs,n)->new ChannelRow(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getLong(4),rs.getLong(5),rs.getString(6)),
                tenant,channel,merchant,Date.valueOf(businessDate));

        Map<String,ChannelRow> byPayment=new HashMap<>();
        Map<String,ChannelRow> byTrade=new HashMap<>();
        for(ChannelRow c:channels){if(c.paymentNo()!=null&&!c.paymentNo().isBlank())byPayment.put(c.paymentNo(),c);byTrade.put(c.trade(),c);}
        Set<Long> used=new HashSet<>(); int matches=0,diffs=0;

        for(LocalRow local:locals){
            ChannelRow channelRow=byPayment.get(local.paymentNo());
            if(channelRow==null&&local.trade()!=null)channelRow=byTrade.get(local.trade());
            if(channelRow!=null)used.add(channelRow.id());
            var result=ReconciliationMatcher.match(
                    new ReconciliationMatcher.LocalFact(local.paymentNo(),local.trade(),local.effectiveAmount(),local.effectiveRefund(),local.status()),
                    channelRow==null?null:new ReconciliationMatcher.ChannelFact(channelRow.paymentNo(),channelRow.trade(),channelRow.amount(),channelRow.refund(),channelRow.status()));
            long detailId=persistDetail(tenant,batchId,local,channelRow,result,now);
            if(result.type()==ReconciliationResultType.MATCH){matches++;createSettlementSource(tenant,detailId,local,businessDate,now);}
            else {diffs++;createDifference(tenant,batchId,detailId,result.type(),now);}
        }
        for(ChannelRow channelRow:channels){
            if(used.contains(channelRow.id()))continue;
            var result=ReconciliationMatcher.match(null,new ReconciliationMatcher.ChannelFact(channelRow.paymentNo(),channelRow.trade(),channelRow.amount(),channelRow.refund(),channelRow.status()));
            long detailId=persistDetail(tenant,batchId,null,channelRow,result,now);
            diffs++;createDifference(tenant,batchId,detailId,result.type(),now);
        }

        jdbc.update("""
            UPDATE finance_reconciliation_batch
            SET status='COMPLETED',local_count=?,channel_count=?,match_count=?,difference_count=?,completed_time=NOW(3)
            WHERE id=?
            """,locals.size(),channels.size(),matches,diffs,batchId);
        return new RunResult(batchNo,locals.size(),channels.size(),matches,diffs);
    }

    private long persistDetail(long tenant,long batchId,LocalRow local,ChannelRow channel,
                               ReconciliationMatcher.Result result,LocalDateTime now){
        long id=ids.nextId();
        jdbc.update("""
            INSERT INTO finance_reconciliation_detail(
              id,tenant_id,batch_id,payment_no,channel_trade_no,
              local_amount_fen,local_original_amount_fen,local_adjustment_fen,channel_amount_fen,
              local_refund_fen,local_original_refund_fen,local_refund_adjustment_fen,channel_refund_fen,
              local_status,channel_status,result_type,difference_amount_fen,create_time
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """,id,tenant,batchId,
                local!=null?local.paymentNo():channel.paymentNo(), channel!=null?channel.trade():local.trade(),
                local!=null?local.effectiveAmount():null,local!=null?local.originalAmount():null,local!=null?local.paymentAdjustment():0,
                channel!=null?channel.amount():null,
                local!=null?local.effectiveRefund():null,local!=null?local.originalRefund():null,local!=null?local.refundAdjustment():0,
                channel!=null?channel.refund():null,
                local!=null?local.status():null,channel!=null?channel.status():null,result.type().name(),result.differenceAmountFen(),now);
        return id;
    }

    private void createDifference(long tenant,long batchId,long detailId,ReconciliationResultType type,LocalDateTime now){
        long id=ids.nextId();
        jdbc.update("""
            INSERT INTO finance_difference_case(
              id,tenant_id,case_no,reconciliation_batch_id,reconciliation_detail_id,difference_type,status,reason,create_time,update_time
            ) VALUES (?,?,?,?,?,?,'OPEN',?,?,?)
            """,id,tenant,"DF"+id,batchId,detailId,type.name(),"Exact reconciliation mismatch: "+type.name(),now,now);
    }

    private void createSettlementSource(long tenant,long detailId,LocalRow local,LocalDate businessDate,LocalDateTime now){
        long base=local.effectiveAmount()-local.effectiveRefund();
        List<ExistingSource> existing=jdbc.query("""
            SELECT id,status,gross_amount_fen,refund_amount_fen,settlement_base_amount_fen
            FROM finance_settlement_source WHERE tenant_id=? AND payment_no=? FOR UPDATE
            """,(rs,n)->new ExistingSource(rs.getLong(1),rs.getString(2),rs.getLong(3),rs.getLong(4),rs.getLong(5)),tenant,local.paymentNo());
        if(existing.isEmpty()){
            long id=ids.nextId();
            jdbc.update("""
                INSERT INTO finance_settlement_source(
                  id,tenant_id,source_no,reconciliation_detail_id,payment_no,biz_order_no,station_id,channel,merchant_id,business_date,
                  gross_amount_fen,refund_amount_fen,settlement_base_amount_fen,currency,status,create_time,update_time
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'READY',?,?)
                """,id,tenant,"SS"+id,detailId,local.paymentNo(),local.orderNo(),local.stationId(),local.channel(),local.merchant(),businessDate,
                    local.effectiveAmount(),local.effectiveRefund(),base,"CNY",now,now);
            return;
        }
        ExistingSource source=existing.get(0);
        if("SETTLED".equals(source.status())){
            if(source.gross()==local.effectiveAmount()&&source.refund()==local.effectiveRefund()&&source.base()==base)return;
            throw new IllegalStateException("settled source changed; create settlement adjustment instead: "+local.paymentNo());
        }
        if("ALLOCATED".equals(source.status())) throw new IllegalStateException("settlement source is awaiting approval: "+local.paymentNo());
        jdbc.update("""
            UPDATE finance_settlement_source
            SET reconciliation_detail_id=?,gross_amount_fen=?,refund_amount_fen=?,settlement_base_amount_fen=?,update_time=?
            WHERE id=? AND status='READY'
            """,detailId,local.effectiveAmount(),local.effectiveRefund(),base,now,source.id());
    }

    private static String required(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+" required");return value.trim();}
}
