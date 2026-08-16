package com.example.evcharging.finance.invoice;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/v1/finance/invoices")
public class InvoiceAdminController {
    private final InvoiceApplicationService service;private final JdbcTemplate jdbc;
    public InvoiceAdminController(InvoiceApplicationService service,JdbcTemplate jdbc){this.service=service;this.jdbc=jdbc;}
    @PostMapping public ApiResponse<Map<String,String>> issue(@RequestBody InvoiceApplicationService.CreateRequest request){return ApiResponse.ok(Map.of("invoiceNo",service.issue(request)));}
    @PostMapping("/{invoiceNo}/red-flush") public ApiResponse<Map<String,String>> red(@PathVariable String invoiceNo,@RequestBody InvoiceApplicationService.RedRequest request){return ApiResponse.ok(Map.of("redNo",service.redFlush(invoiceNo,request)));}
    @GetMapping public ApiResponse<List<InvoiceView>> list(@RequestParam(defaultValue="100")int limit){long t=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,200));return ApiResponse.ok(jdbc.query("SELECT invoice_no,payment_no,biz_order_no,amount_fen,status,pdf_url,issued_time FROM finance_invoice WHERE tenant_id=? ORDER BY id DESC LIMIT ?",(rs,n)->new InvoiceView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getLong(4),rs.getString(5),rs.getString(6),String.valueOf(rs.getObject(7))),t,size));}
    public record InvoiceView(String invoiceNo,String paymentNo,String orderNo,long amountFen,String status,String pdfUrl,String issuedTime){}
}
