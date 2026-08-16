package com.example.evcharging.payment.application;

import com.alibaba.csp.sentinel.annotation.SentinelResource;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.payment.domain.*;
import com.example.evcharging.payment.gateway.PaymentGateway;
import com.example.evcharging.payment.integration.CoreOrderClient;
import com.example.evcharging.payment.integration.OrderPaymentSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentApplicationService {
  private final JdbcTemplate jdbc; private final IdGenerator ids; private final CoreOrderClient core; private final Map<PaymentChannel,PaymentGateway> gateways; private final ObjectMapper mapper; private final TransactionTemplate tx;
  public PaymentApplicationService(JdbcTemplate jdbc,IdGenerator ids,CoreOrderClient core,List<PaymentGateway> gateways,ObjectMapper mapper,PlatformTransactionManager transactionManager){
    this.jdbc=jdbc;this.ids=ids;this.core=core;this.mapper=mapper;this.tx=new TransactionTemplate(transactionManager);Map<PaymentChannel,PaymentGateway> m=new EnumMap<>(PaymentChannel.class);gateways.forEach(g->m.put(g.channel(),g));this.gateways=Map.copyOf(m);
  }

  @SentinelResource("payment.create")
  public CreatePaymentResult create(CreatePaymentRequest request){
    long tenant=RequestContext.requireTenantId();
    CreatePaymentResult known=findByRequest(tenant,request.requestId());if(known!=null)return known;
    OrderPaymentSnapshot order=core.paymentSnapshot(request.orderNo());long amount=order.outstandingAmountFen();if(amount<=0)throw new IllegalStateException("order has no outstanding amount");
    PaymentChannel channel=PaymentChannel.valueOf(request.channel().toUpperCase(Locale.ROOT));PaymentGateway gateway=Optional.ofNullable(gateways.get(channel)).orElseThrow(()->new IllegalArgumentException("payment channel not configured: "+channel));
    long id=ids.nextId();String paymentNo="PO"+id;LocalDateTime now=LocalDateTime.now();
    Boolean owner=tx.execute(status->{try{jdbc.update("INSERT INTO payment_order(id,tenant_id,payment_no,request_id,biz_type,biz_order_no,station_id,user_id,channel,amount_fen,currency,status,create_time,update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,'CREATED',?,?)",id,tenant,paymentNo,request.requestId(),"CHARGE_ORDER",order.orderNo(),order.stationId(),order.userId(),channel.name(),amount,"CNY",now,now);jdbc.update("INSERT INTO payment_active_order(tenant_id,biz_order_no,payment_id,payment_no,create_time) VALUES (?,?,?,?,?)",tenant,order.orderNo(),id,paymentNo,now);return true;}catch(DuplicateKeyException duplicate){status.setRollbackOnly();return false;}});
    if(!Boolean.TRUE.equals(owner)){CreatePaymentResult duplicate=findByRequest(tenant,request.requestId());if(duplicate!=null)return duplicate;return findActive(tenant,order.orderNo());}
    PaymentGateway.CreateResult remote;
    try{remote=gateway.create(paymentNo,amount,"CNY");}
    catch(Exception channelError){tx.executeWithoutResult(s->jdbc.update("UPDATE payment_order SET status='UNKNOWN',update_time=?,version=version+1 WHERE id=? AND status='CREATED'",LocalDateTime.now(),id));return new CreatePaymentResult(paymentNo,order.orderNo(),channel.name(),PaymentStatus.UNKNOWN.name(),amount,null);}
    tx.executeWithoutResult(s->jdbc.update("UPDATE payment_order SET status=?,channel_trade_no=?,payment_token=?,update_time=?,version=version+1 WHERE id=? AND status='CREATED'",remote.status().name(),remote.channelTradeNo(),remote.paymentToken(),LocalDateTime.now(),id));
    return new CreatePaymentResult(paymentNo,order.orderNo(),channel.name(),remote.status().name(),amount,remote.paymentToken());
  }

  private CreatePaymentResult findByRequest(long tenant,String requestId){List<CreatePaymentResult> rows=jdbc.query("SELECT payment_no,biz_order_no,channel,status,amount_fen,payment_token FROM payment_order WHERE tenant_id=? AND request_id=?",(rs,n)->new CreatePaymentResult(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getLong(5),rs.getString(6)),tenant,requestId);return rows.isEmpty()?null:rows.getFirst();}
  private CreatePaymentResult findActive(long tenant,String orderNo){return jdbc.queryForObject("SELECT p.payment_no,p.biz_order_no,p.channel,p.status,p.amount_fen,p.payment_token FROM payment_active_order a JOIN payment_order p ON p.id=a.payment_id WHERE a.tenant_id=? AND a.biz_order_no=?",(rs,n)->new CreatePaymentResult(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getLong(5),rs.getString(6)),tenant,orderNo);}

  @Transactional
  public String acceptSuccess(String paymentNo,String callbackId,String channelTradeNo){
    long tenant=RequestContext.requireTenantId();LocalDateTime now=LocalDateTime.now();String fp="SUCCESS:"+callbackId;
    try{jdbc.update("INSERT INTO payment_callback_log(id,tenant_id,callback_fingerprint,payment_no,channel,callback_status,raw_payload,processed_time) SELECT ?,tenant_id,?,?,channel,'SUCCESS',?,? FROM payment_order WHERE tenant_id=? AND payment_no=?",ids.nextId(),fp,paymentNo,"channelTradeNo="+channelTradeNo,now,tenant,paymentNo);}catch(DuplicateKeyException duplicate){return "IDEMPOTENT_SUCCESS";}
    Map<String,Object> p=jdbc.queryForMap("SELECT id,biz_order_no,station_id,amount_fen,channel,status FROM payment_order WHERE tenant_id=? AND payment_no=? FOR UPDATE",tenant,paymentNo);PaymentStatus status=PaymentStatus.valueOf(String.valueOf(p.get("status")));if(status==PaymentStatus.SUCCESS)return "IDEMPOTENT_SUCCESS";if(!PaymentStateMachine.canSucceed(status))throw new IllegalStateException("payment cannot succeed from "+status);
    long amount=((Number)p.get("amount_fen")).longValue();long paymentId=((Number)p.get("id")).longValue();String channel=String.valueOf(p.get("channel"));
    jdbc.update("UPDATE payment_order SET status='SUCCESS',channel_trade_no=?,success_time=?,update_time=?,version=version+1 WHERE id=?",channelTradeNo,now,now,paymentId);
    jdbc.update("INSERT INTO payment_transaction(id,tenant_id,payment_id,payment_no,transaction_type,channel,channel_trade_no,amount_fen,channel_status,occurred_time,create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?)",ids.nextId(),tenant,paymentId,paymentNo,"PAYMENT",channel,channelTradeNo,amount,"SUCCESS",now,now);
    emit(tenant,paymentNo,"payment.payment.succeeded",Map.of("paymentNo",paymentNo,"orderNo",String.valueOf(p.get("biz_order_no")),"stationId",((Number)p.get("station_id")).longValue(),"amountFen",amount,"channel",channel,"channelTradeNo",channelTradeNo,"successTime",now.toString()));return "SUCCESS";
  }

  @Transactional
  @SentinelResource("payment.refund")
  public String createRefund(CreateRefundRequest request){long tenant=RequestContext.requireTenantId();List<String> existing=jdbc.query("SELECT refund_no FROM payment_refund WHERE tenant_id=? AND request_id=?",(rs,n)->rs.getString(1),tenant,request.requestId());if(!existing.isEmpty())return existing.getFirst();Map<String,Object> p=jdbc.queryForMap("SELECT id,station_id,amount_fen,refunded_amount_fen,refund_reserved_fen,status FROM payment_order WHERE tenant_id=? AND payment_no=? FOR UPDATE",tenant,request.paymentNo());if(!"SUCCESS".equals(p.get("status")))throw new IllegalStateException("only successful payment can refund");long available=((Number)p.get("amount_fen")).longValue()-((Number)p.get("refunded_amount_fen")).longValue()-((Number)p.get("refund_reserved_fen")).longValue();if(request.amountFen()>available)throw new IllegalArgumentException("refund exceeds refundable amount");long id=ids.nextId();String refundNo="RF"+id;LocalDateTime now=LocalDateTime.now();long paymentId=((Number)p.get("id")).longValue();jdbc.update("UPDATE payment_order SET refund_reserved_fen=refund_reserved_fen+?,update_time=?,version=version+1 WHERE id=?",request.amountFen(),now,paymentId);jdbc.update("INSERT INTO payment_refund(id,tenant_id,refund_no,request_id,payment_id,payment_no,station_id,amount_fen,reason,status,create_time,update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",id,tenant,refundNo,request.requestId(),paymentId,request.paymentNo(),((Number)p.get("station_id")).longValue(),request.amountFen(),request.reason(),RefundStatus.CREATED.name(),now,now);return refundNo;}

  @Transactional
  public String acceptRefundSuccess(String refundNo,String channelRefundNo){long tenant=RequestContext.requireTenantId();Map<String,Object> r=jdbc.queryForMap("SELECT id,payment_id,payment_no,station_id,amount_fen,status FROM payment_refund WHERE tenant_id=? AND refund_no=? FOR UPDATE",tenant,refundNo);if("SUCCESS".equals(r.get("status")))return "IDEMPOTENT_SUCCESS";long amount=((Number)r.get("amount_fen")).longValue();long paymentId=((Number)r.get("payment_id")).longValue();LocalDateTime now=LocalDateTime.now();jdbc.update("UPDATE payment_refund SET status='SUCCESS',channel_refund_no=?,success_time=?,update_time=? WHERE id=?",channelRefundNo,now,now,r.get("id"));int updated=jdbc.update("UPDATE payment_order SET refund_reserved_fen=refund_reserved_fen-?,refunded_amount_fen=refunded_amount_fen+?,update_time=?,version=version+1 WHERE id=? AND refund_reserved_fen>=?",amount,amount,now,paymentId,amount);if(updated!=1)throw new IllegalStateException("refund reservation lost");String orderNo=jdbc.queryForObject("SELECT biz_order_no FROM payment_order WHERE id=?",String.class,paymentId);emit(tenant,refundNo,"payment.refund.succeeded",Map.of("refundNo",refundNo,"paymentNo",String.valueOf(r.get("payment_no")),"orderNo",orderNo,"stationId",((Number)r.get("station_id")).longValue(),"amountFen",amount,"channelRefundNo",channelRefundNo,"successTime",now.toString()));return "SUCCESS";}

  @Transactional
  public void markQueryResult(String paymentNo,PaymentGateway.QueryResult result){long tenant=RequestContext.requireTenantId();if(result.status()==PaymentStatus.SUCCESS)acceptSuccess(paymentNo,"QUERY:"+paymentNo+":"+String.valueOf(result.channelTradeNo()),String.valueOf(result.channelTradeNo()));else if(result.status()==PaymentStatus.FAILED||result.status()==PaymentStatus.CLOSED){jdbc.update("UPDATE payment_order SET status=?,update_time=?,version=version+1 WHERE tenant_id=? AND payment_no=? AND status='UNKNOWN'",result.status().name(),LocalDateTime.now(),tenant,paymentNo);jdbc.update("DELETE a FROM payment_active_order a JOIN payment_order p ON p.id=a.payment_id WHERE p.tenant_id=? AND p.payment_no=? AND p.status IN ('FAILED','CLOSED')",tenant,paymentNo);}}

  private void emit(long tenant,String aggregateId,String type,Object payload){try{long id=ids.nextId();String eventId="EV"+id;String json=mapper.writeValueAsString(payload);LocalDateTime now=LocalDateTime.now();jdbc.update("INSERT INTO payment_event_outbox(id,event_id,aggregate_id,event_type,event_version,tenant_id,payload,status,retry_count,next_retry_time,occurred_time,create_time) VALUES (?,?,?,?,?,?,?,'NEW',0,?,?,?)",id,eventId,aggregateId,type,"1.0",tenant,json,now,now,now);}catch(Exception e){throw new IllegalStateException("cannot create payment event",e);}}
}
