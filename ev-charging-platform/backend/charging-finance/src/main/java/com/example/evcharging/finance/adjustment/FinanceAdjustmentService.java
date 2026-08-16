package com.example.evcharging.finance.adjustment;

import com.example.evcharging.finance.ledger.LedgerPosting;
import com.example.evcharging.finance.ledger.LedgerPostingService;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FinanceAdjustmentService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final LedgerPostingService ledger;

    public FinanceAdjustmentService(JdbcTemplate jdbc, IdGenerator ids, LedgerPostingService ledger) {
        this.jdbc = jdbc;
        this.ids = ids;
        this.ledger = ledger;
    }

    public enum AdjustmentType { PAYMENT_AMOUNT, REFUND_AMOUNT }
    public record CreateRequest(String requestId, AdjustmentType type, String paymentNo, long amountFen, String reason) {}
    public record AdjustmentView(String adjustmentNo, String type, String paymentNo, long amountFen, String status,
                                 String reason, long createdBy, Long approvedBy, String createTime) {}

    @Transactional
    public String create(CreateRequest request) {
        long tenant = RequestContext.requireTenantId();
        long user = RequestContext.requireUserId();
        if (request.requestId() == null || request.requestId().isBlank()) throw new IllegalArgumentException("requestId required");
        if (request.type() == null) throw new IllegalArgumentException("type required");
        if (request.paymentNo() == null || request.paymentNo().isBlank()) throw new IllegalArgumentException("paymentNo required");
        if (request.amountFen() == 0) throw new IllegalArgumentException("amountFen cannot be zero");
        if (request.reason() == null || request.reason().isBlank()) throw new IllegalArgumentException("reason required");
        Integer paymentExists = jdbc.queryForObject("SELECT COUNT(*) FROM finance_transaction_fact WHERE tenant_id=? AND payment_no=?",
                Integer.class, tenant, request.paymentNo());
        if (paymentExists == null || paymentExists == 0) throw new IllegalArgumentException("payment fact not found");

        List<String> prior = jdbc.query("SELECT adjustment_no FROM finance_adjustment_order WHERE tenant_id=? AND request_id=?",
                (rs,n) -> rs.getString(1), tenant, request.requestId());
        if (!prior.isEmpty()) return prior.get(0);

        long id = ids.nextId();
        String no = "ADJ" + id;
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("""
            INSERT INTO finance_adjustment_order(
              id,tenant_id,adjustment_no,request_id,adjustment_type,payment_no,amount_fen,reason,status,
              created_by,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,'PENDING_APPROVAL',?,?,?)
            """, id,tenant,no,request.requestId(),request.type().name(),request.paymentNo(),request.amountFen(),
                request.reason(),user,now,now);
        return no;
    }

    @Transactional
    public String reverse(String adjustmentNo, String requestId, String reason) {
        long tenant = RequestContext.requireTenantId();
        long user = RequestContext.requireUserId();
        if(requestId==null||requestId.isBlank())throw new IllegalArgumentException("requestId required");
        if(reason==null||reason.isBlank())throw new IllegalArgumentException("reason required");
        AdjustmentRow original = lock(tenant, adjustmentNo);
        if (!"POSTED".equals(original.status())) throw new IllegalStateException("only POSTED adjustment can be reversed");
        List<String> prior = jdbc.query("SELECT adjustment_no FROM finance_adjustment_order WHERE tenant_id=? AND request_id=?",
                (rs,n)->rs.getString(1),tenant,requestId);
        if(!prior.isEmpty()) return prior.get(0);
        Integer reversalCount=jdbc.queryForObject("SELECT COUNT(*) FROM finance_adjustment_order WHERE tenant_id=? AND reverses_adjustment_id=? AND status IN ('PENDING_APPROVAL','POSTED')",Integer.class,tenant,original.id());
        if(reversalCount!=null&&reversalCount>0)throw new IllegalStateException("adjustment already has an active reversal");
        long id=ids.nextId(); String no="ADJ"+id; LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            INSERT INTO finance_adjustment_order(
              id,tenant_id,adjustment_no,request_id,adjustment_type,payment_no,amount_fen,reason,status,
              reverses_adjustment_id,created_by,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,'PENDING_APPROVAL',?,?,?,?)
            """, id,tenant,no,requestId,original.type(),original.paymentNo(),Math.negateExact(original.amountFen()),
                reason,original.id(),user,now,now);
        return no;
    }

    @Transactional
    public void approve(String adjustmentNo) {
        long tenant = RequestContext.requireTenantId();
        long approver = RequestContext.requireUserId();
        AdjustmentRow row = lock(tenant, adjustmentNo);
        if ("POSTED".equals(row.status())) return;
        if (!"PENDING_APPROVAL".equals(row.status())) throw new IllegalStateException("adjustment is not pending approval");
        if (row.createdBy() == approver) throw new IllegalStateException("maker-checker violation: creator cannot approve");

        validateEffectiveBalance(tenant, row);
        LedgerPosting posting = adjustmentPosting(row.type(), row.amountFen(), row.paymentNo());
        LocalDateTime now = LocalDateTime.now();
        ledger.post(tenant, "ADJUSTMENT:" + adjustmentNo, "finance.adjustment.posted", adjustmentNo, now, posting);
        jdbc.update("""
            UPDATE finance_adjustment_order
            SET status='POSTED',approved_by=?,approved_time=?,posted_time=?,update_time=?
            WHERE id=? AND status='PENDING_APPROVAL'
            """, approver,now,now,now,row.id());
    }

    @Transactional
    public void reject(String adjustmentNo) {
        long tenant=RequestContext.requireTenantId(); long approver=RequestContext.requireUserId();
        AdjustmentRow row=lock(tenant,adjustmentNo);
        if("REJECTED".equals(row.status())) return;
        if(!"PENDING_APPROVAL".equals(row.status())) throw new IllegalStateException("adjustment is not pending approval");
        if(row.createdBy()==approver) throw new IllegalStateException("maker-checker violation: creator cannot reject own adjustment");
        LocalDateTime now=LocalDateTime.now();
        jdbc.update("UPDATE finance_adjustment_order SET status='REJECTED',rejected_by=?,rejected_time=?,update_time=? WHERE id=? AND status='PENDING_APPROVAL'",
                approver,now,now,row.id());
    }

    public List<AdjustmentView> list(String status) {
        long tenant=RequestContext.requireTenantId();
        String sql="SELECT adjustment_no,adjustment_type,payment_no,amount_fen,status,reason,created_by,approved_by,create_time FROM finance_adjustment_order WHERE tenant_id=?"+
                (status==null||status.isBlank()?"":" AND status=?")+" ORDER BY id DESC LIMIT 200";
        Object[] args=status==null||status.isBlank()?new Object[]{tenant}:new Object[]{tenant,status.toUpperCase()};
        return jdbc.query(sql,(rs,n)->new AdjustmentView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getLong(4),rs.getString(5),
                rs.getString(6),rs.getLong(7),(Long)rs.getObject(8),String.valueOf(rs.getObject(9))),args);
    }

    private AdjustmentRow lock(long tenant,String no){
        List<AdjustmentRow> rows=jdbc.query("SELECT id,adjustment_type,payment_no,amount_fen,status,created_by FROM finance_adjustment_order WHERE tenant_id=? AND adjustment_no=? FOR UPDATE",
                (rs,n)->new AdjustmentRow(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getLong(4),rs.getString(5),rs.getLong(6)),tenant,no);
        if(rows.isEmpty()) throw new IllegalArgumentException("adjustment not found"); return rows.get(0);
    }
    private record AdjustmentRow(long id,String type,String paymentNo,long amountFen,String status,long createdBy){}

    private void validateEffectiveBalance(long tenant, AdjustmentRow candidate) {
        Long originalPayment = jdbc.queryForObject("SELECT amount_fen FROM finance_transaction_fact WHERE tenant_id=? AND payment_no=?", Long.class, tenant, candidate.paymentNo());
        long paymentAdjustment = jdbc.queryForObject("SELECT COALESCE(SUM(amount_fen),0) FROM finance_adjustment_order WHERE tenant_id=? AND payment_no=? AND adjustment_type='PAYMENT_AMOUNT' AND status='POSTED'", Long.class, tenant, candidate.paymentNo());
        long originalRefund = jdbc.queryForObject("SELECT COALESCE(SUM(amount_fen),0) FROM finance_refund_fact WHERE tenant_id=? AND payment_no=? AND refund_status='SUCCESS'", Long.class, tenant, candidate.paymentNo());
        long refundAdjustment = jdbc.queryForObject("SELECT COALESCE(SUM(amount_fen),0) FROM finance_adjustment_order WHERE tenant_id=? AND payment_no=? AND adjustment_type='REFUND_AMOUNT' AND status='POSTED'", Long.class, tenant, candidate.paymentNo());
        long effectivePayment = Math.addExact(originalPayment == null ? 0 : originalPayment, paymentAdjustment);
        long effectiveRefund = Math.addExact(originalRefund, refundAdjustment);
        if ("PAYMENT_AMOUNT".equals(candidate.type())) effectivePayment = Math.addExact(effectivePayment, candidate.amountFen());
        else effectiveRefund = Math.addExact(effectiveRefund, candidate.amountFen());
        if (effectivePayment < 0 || effectiveRefund < 0 || effectiveRefund > effectivePayment) throw new IllegalStateException("adjustment would create invalid financial fact");
    }

    private LedgerPosting adjustmentPosting(String type,long amount,String paymentNo){
        long absolute=Math.abs(amount); boolean positive=amount>0;
        if("PAYMENT_AMOUNT".equals(type)){
            return positive ? new LedgerPosting(List.of(
                    new LedgerPosting.Entry("PAYMENT_CHANNEL_RECEIVABLE","PAYMENT",paymentNo,LedgerPosting.Side.DEBIT,absolute),
                    new LedgerPosting.Entry("CHARGING_RECEIVABLE_CLEARING","PAYMENT",paymentNo,LedgerPosting.Side.CREDIT,absolute)))
                    : new LedgerPosting(List.of(
                    new LedgerPosting.Entry("CHARGING_RECEIVABLE_CLEARING","PAYMENT",paymentNo,LedgerPosting.Side.DEBIT,absolute),
                    new LedgerPosting.Entry("PAYMENT_CHANNEL_RECEIVABLE","PAYMENT",paymentNo,LedgerPosting.Side.CREDIT,absolute)));
        }
        if("REFUND_AMOUNT".equals(type)){
            return positive ? new LedgerPosting(List.of(
                    new LedgerPosting.Entry("CHARGING_RECEIVABLE_CLEARING","PAYMENT",paymentNo,LedgerPosting.Side.DEBIT,absolute),
                    new LedgerPosting.Entry("PAYMENT_CHANNEL_RECEIVABLE","PAYMENT",paymentNo,LedgerPosting.Side.CREDIT,absolute)))
                    : new LedgerPosting(List.of(
                    new LedgerPosting.Entry("PAYMENT_CHANNEL_RECEIVABLE","PAYMENT",paymentNo,LedgerPosting.Side.DEBIT,absolute),
                    new LedgerPosting.Entry("CHARGING_RECEIVABLE_CLEARING","PAYMENT",paymentNo,LedgerPosting.Side.CREDIT,absolute)));
        }
        throw new IllegalArgumentException("unsupported adjustment type: "+type);
    }
}
