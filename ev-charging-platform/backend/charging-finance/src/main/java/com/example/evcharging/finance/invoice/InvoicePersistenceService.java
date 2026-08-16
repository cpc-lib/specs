package com.example.evcharging.finance.invoice;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoicePersistenceService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;
    public InvoicePersistenceService(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    public record ReserveResult(long id,String requestNo,String paymentNo,String orderNo,long amountFen,String titleType,String titleName,String taxNo,String email,String providerCode,String status,String invoiceNo){}
    public record RedReserve(long id,String redNo,long invoiceId,String invoiceNo,String providerInvoiceNo,String reason,String status){}

    @Transactional
    public ReserveResult reserve(InvoiceApplicationService.CreateRequest request){
        long tenant=RequestContext.requireTenantId();
        String requestId=required(request.requestId(),"requestId");
        List<ReserveResult> prior=jdbc.query("SELECT r.id,r.request_no,r.payment_no,r.biz_order_no,r.amount_fen,r.title_type,r.title_name,r.tax_no,r.email,r.provider_code,r.status,i.invoice_no FROM finance_invoice_request r LEFT JOIN finance_invoice i ON i.request_id=r.id WHERE r.tenant_id=? AND r.request_id=?",
                (rs,n)->new ReserveResult(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getLong(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9),rs.getString(10),rs.getString(11),rs.getString(12)),tenant,requestId);
        if(!prior.isEmpty()){
            ReserveResult existing=prior.get(0);
            if("FAILED".equals(existing.status())){
                jdbc.update("UPDATE finance_invoice_request SET status='PROCESSING',failure_message=NULL,update_time=? WHERE id=? AND status='FAILED'",LocalDateTime.now(),existing.id());
                return new ReserveResult(existing.id(),existing.requestNo(),existing.paymentNo(),existing.orderNo(),existing.amountFen(),existing.titleType(),existing.titleName(),existing.taxNo(),existing.email(),existing.providerCode(),"PROCESSING",existing.invoiceNo());
            }
            return existing;
        }
        String paymentNo=required(request.paymentNo(),"paymentNo");
        Fact fact=loadFact(tenant,paymentNo);
        long netAmount=effectiveNetAmount(tenant,paymentNo,fact.amountFen());
        if(netAmount<=0)throw new IllegalStateException("no invoiceable amount after refunds/adjustments");
        Integer active=jdbc.queryForObject("SELECT COUNT(*) FROM finance_invoice WHERE tenant_id=? AND payment_no=? AND status='ISSUED'",Integer.class,tenant,paymentNo);
        if(active!=null&&active>0)throw new IllegalStateException("active invoice already exists for payment");
        String titleType=required(request.titleType(),"titleType").toUpperCase();
        String titleName=required(request.titleName(),"titleName");
        if("ENTERPRISE".equals(titleType)&&(request.taxNo()==null||request.taxNo().isBlank()))throw new IllegalArgumentException("taxNo required for enterprise invoice");
        long id=ids.nextId();String requestNo="IR"+id;LocalDateTime now=LocalDateTime.now();
        try {
            jdbc.update("INSERT INTO finance_invoice_active(tenant_id,payment_no,invoice_request_id,create_time) VALUES (?,?,?,?)",tenant,paymentNo,id,now);
        } catch (DuplicateKeyException duplicate) {
            throw new IllegalStateException("another active invoice request exists for payment",duplicate);
        }
        jdbc.update("""
            INSERT INTO finance_invoice_request(
              id,tenant_id,request_no,request_id,payment_no,biz_order_no,amount_fen,title_type,title_name,tax_no,email,
              provider_code,status,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'PROCESSING',?,?)
            """,id,tenant,requestNo,requestId,paymentNo,fact.orderNo(),netAmount,titleType,titleName,blank(request.taxNo()),blank(request.email()),
                request.providerCode()==null||request.providerCode().isBlank()?"MOCK":request.providerCode().toUpperCase(),now,now);
        return new ReserveResult(id,requestNo,paymentNo,fact.orderNo(),netAmount,titleType,titleName,blank(request.taxNo()),blank(request.email()),
                request.providerCode()==null||request.providerCode().isBlank()?"MOCK":request.providerCode().toUpperCase(),"PROCESSING",null);
    }

    @Transactional
    public String completeIssue(ReserveResult reserved,InvoiceProvider.IssueResult result){
        long tenant=RequestContext.requireTenantId();
        List<String> existing=jdbc.query("SELECT invoice_no FROM finance_invoice WHERE tenant_id=? AND request_id=?",(rs,n)->rs.getString(1),tenant,reserved.id());
        if(!existing.isEmpty())return existing.get(0);
        long id=ids.nextId();String invoiceNo="INV"+id;LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            INSERT INTO finance_invoice(
              id,tenant_id,request_id,invoice_no,provider_invoice_no,payment_no,biz_order_no,amount_fen,status,pdf_url,
              issued_time,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?, 'ISSUED',?,?,?,?)
            """,id,tenant,reserved.id(),invoiceNo,result.providerInvoiceNo(),reserved.paymentNo(),reserved.orderNo(),reserved.amountFen(),result.pdfUrl(),now,now,now);
        jdbc.update("UPDATE finance_invoice_request SET status='SUCCESS',failure_message=NULL,update_time=? WHERE id=? AND status='PROCESSING'",now,reserved.id());
        return invoiceNo;
    }

    @Transactional
    public void failIssue(long requestDbId,String message){jdbc.update("UPDATE finance_invoice_request SET status='FAILED',failure_message=?,update_time=? WHERE id=? AND status='PROCESSING'",trim(message,500),LocalDateTime.now(),requestDbId);}

    @Transactional
    public RedReserve reserveRed(String invoiceNo,String requestId,String reason){
        long tenant=RequestContext.requireTenantId();
        List<RedReserve> prior=jdbc.query("""
            SELECT r.id,r.red_no,r.invoice_id,i.invoice_no,i.provider_invoice_no,r.reason,r.status
            FROM finance_invoice_red_flush r JOIN finance_invoice i ON i.id=r.invoice_id
            WHERE r.tenant_id=? AND r.request_id=?
            """,(rs,n)->new RedReserve(rs.getLong(1),rs.getString(2),rs.getLong(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7)),tenant,requestId);
        if(!prior.isEmpty()){
            RedReserve existing=prior.get(0);
            if("FAILED".equals(existing.status())){
                jdbc.update("UPDATE finance_invoice_red_flush SET status='PROCESSING',completed_time=NULL WHERE id=? AND status='FAILED'",existing.id());
                return new RedReserve(existing.id(),existing.redNo(),existing.invoiceId(),existing.invoiceNo(),existing.providerInvoiceNo(),existing.reason(),"PROCESSING");
            }
            return existing;
        }
        List<InvoiceRow> invoices=jdbc.query("SELECT id,provider_invoice_no,status FROM finance_invoice WHERE tenant_id=? AND invoice_no=? FOR UPDATE",
                (rs,n)->new InvoiceRow(rs.getLong(1),rs.getString(2),rs.getString(3)),tenant,invoiceNo);
        if(invoices.isEmpty())throw new IllegalArgumentException("invoice not found");InvoiceRow invoice=invoices.get(0);
        if("RED_FLUSHED".equals(invoice.status()))throw new IllegalStateException("invoice already red flushed");
        if(!"ISSUED".equals(invoice.status()))throw new IllegalStateException("invoice is not issued");
        long id=ids.nextId();String redNo="RED"+id;LocalDateTime now=LocalDateTime.now();
        jdbc.update("INSERT INTO finance_invoice_red_flush(id,tenant_id,red_no,request_id,invoice_id,reason,status,create_time) VALUES (?,?,?,?,?,?,'PROCESSING',?)",
                id,tenant,redNo,requestId,invoice.id(),required(reason,"reason"),now);
        return new RedReserve(id,redNo,invoice.id(),invoiceNo,invoice.providerInvoiceNo(),reason,"PROCESSING");
    }

    @Transactional
    public void completeRed(RedReserve reserve,InvoiceProvider.RedResult result){
        long tenant=RequestContext.requireTenantId();LocalDateTime now=LocalDateTime.now();
        jdbc.update("UPDATE finance_invoice_red_flush SET status='SUCCESS',provider_red_no=?,completed_time=? WHERE id=? AND tenant_id=? AND status='PROCESSING'",
                result.providerRedNo(),now,reserve.id(),tenant);
        jdbc.update("UPDATE finance_invoice SET status='RED_FLUSHED',update_time=? WHERE id=? AND tenant_id=? AND status='ISSUED'",now,reserve.invoiceId(),tenant);
        jdbc.update("DELETE FROM finance_invoice_active WHERE tenant_id=? AND payment_no=(SELECT payment_no FROM finance_invoice WHERE id=?)",tenant,reserve.invoiceId());
    }

    @Transactional
    public void failRed(long redId){jdbc.update("UPDATE finance_invoice_red_flush SET status='FAILED',completed_time=? WHERE id=? AND status='PROCESSING'",LocalDateTime.now(),redId);}

    private Fact loadFact(long tenant,String paymentNo){List<Fact> facts=jdbc.query("SELECT biz_order_no,amount_fen FROM finance_transaction_fact WHERE tenant_id=? AND payment_no=? AND payment_status='SUCCESS'",(rs,n)->new Fact(rs.getString(1),rs.getLong(2)),tenant,paymentNo);if(facts.isEmpty())throw new IllegalArgumentException("successful payment fact not found");return facts.get(0);}
    private long effectiveNetAmount(long tenant,String paymentNo,long original){
        Long paymentAdj=jdbc.queryForObject("SELECT COALESCE(SUM(amount_fen),0) FROM finance_adjustment_order WHERE tenant_id=? AND payment_no=? AND adjustment_type='PAYMENT_AMOUNT' AND status='POSTED'",Long.class,tenant,paymentNo);
        Long refunds=jdbc.queryForObject("SELECT COALESCE(SUM(amount_fen),0) FROM finance_refund_fact WHERE tenant_id=? AND payment_no=? AND refund_status='SUCCESS'",Long.class,tenant,paymentNo);
        Long refundAdj=jdbc.queryForObject("SELECT COALESCE(SUM(amount_fen),0) FROM finance_adjustment_order WHERE tenant_id=? AND payment_no=? AND adjustment_type='REFUND_AMOUNT' AND status='POSTED'",Long.class,tenant,paymentNo);
        return Math.subtractExact(Math.addExact(original,paymentAdj==null?0:paymentAdj),Math.addExact(refunds==null?0:refunds,refundAdj==null?0:refundAdj));
    }
    private record Fact(String orderNo,long amountFen){}private record InvoiceRow(long id,String providerInvoiceNo,String status){}
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" required");return v.trim();}
    private static String blank(String v){return v==null||v.isBlank()?null:v.trim();}
    private static String trim(String v,int max){if(v==null)return null;return v.length()<=max?v:v.substring(0,max);}
}
