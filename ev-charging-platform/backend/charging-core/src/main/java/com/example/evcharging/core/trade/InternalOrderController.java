package com.example.evcharging.core.trade;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/internal-api/v1/core/orders")
public class InternalOrderController {
  private final JdbcTemplate jdbc; public InternalOrderController(JdbcTemplate jdbc){this.jdbc=jdbc;}
  @GetMapping("/{orderNo}/payment-snapshot") public OrderPaymentSnapshot snapshot(@PathVariable String orderNo){long tenant=RequestContext.requireTenantId();return jdbc.queryForObject("SELECT order_no,user_id,station_id,receivable_amount_fen,paid_amount_fen,trade_status,payment_status FROM charge_order WHERE tenant_id=? AND order_no=?",(rs,n)->new OrderPaymentSnapshot(rs.getString(1),rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getLong(5),String.valueOf(rs.getInt(6)),String.valueOf(rs.getInt(7))),tenant,orderNo);}
}
