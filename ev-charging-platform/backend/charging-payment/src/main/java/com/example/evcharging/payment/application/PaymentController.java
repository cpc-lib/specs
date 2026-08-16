package com.example.evcharging.payment.application;
import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
public class PaymentController {
  private final PaymentApplicationService service; private final JdbcTemplate jdbc;
  public PaymentController(PaymentApplicationService service,JdbcTemplate jdbc){this.service=service;this.jdbc=jdbc;}
  @PostMapping("/app-api/v1/payments") public ApiResponse<CreatePaymentResult> create(@Valid @RequestBody CreatePaymentRequest r){return ApiResponse.ok(service.create(r));}
  @GetMapping("/admin-api/v1/payments") public ApiResponse<List<PaymentView>> list(@RequestParam(defaultValue="50") int limit){long tenant=RequestContext.requireTenantId();int size=Math.max(1,Math.min(limit,200));return ApiResponse.ok(jdbc.query("SELECT payment_no,biz_order_no,channel,amount_fen,status,refunded_amount_fen,refund_reserved_fen,create_time FROM payment_order WHERE tenant_id=? ORDER BY id DESC LIMIT ?",(rs,n)->new PaymentView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getLong(4),rs.getString(5),rs.getLong(6),rs.getLong(7),String.valueOf(rs.getObject(8))),tenant,size));}
  @PostMapping("/admin-api/v1/payments/{paymentNo}/mock-success") public ApiResponse<String> mockSuccess(@PathVariable String paymentNo,@RequestParam String callbackId,@RequestParam(defaultValue="MOCK-TRADE") String channelTradeNo){return ApiResponse.ok(service.acceptSuccess(paymentNo,callbackId,channelTradeNo));}
  @PostMapping("/admin-api/v1/refunds") public ApiResponse<String> refund(@Valid @RequestBody CreateRefundRequest r){return ApiResponse.ok(service.createRefund(r));}
  @PostMapping("/admin-api/v1/refunds/{refundNo}/mock-success") public ApiResponse<String> refundSuccess(@PathVariable String refundNo,@RequestParam(defaultValue="MOCK-REFUND") String channelRefundNo){return ApiResponse.ok(service.acceptRefundSuccess(refundNo,channelRefundNo));}
  public record PaymentView(String paymentNo,String orderNo,String channel,long amountFen,String status,long refundedFen,long refundReservedFen,String createTime){}
}
