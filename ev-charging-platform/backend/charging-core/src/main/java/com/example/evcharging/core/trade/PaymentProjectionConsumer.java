package com.example.evcharging.core.trade;
import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Component
public class PaymentProjectionConsumer {
  private final JdbcTemplate jdbc;private final ObjectMapper mapper;private final IdGenerator ids;
  public PaymentProjectionConsumer(JdbcTemplate jdbc,ObjectMapper mapper,IdGenerator ids){this.jdbc=jdbc;this.mapper=mapper;this.ids=ids;}
  @KafkaListener(topics="${charging.payment-event-topic:ev.payment.payment.v1}",groupId="charging-core-payment-projection-v1")
  @Transactional public void consume(String raw)throws Exception{
    DomainEventEnvelope<JsonNode> e=mapper.readValue(raw,new TypeReference<DomainEventEnvelope<JsonNode>>(){});if(e.payload()==null)throw new IllegalArgumentException("payment event payload missing");
    try{jdbc.update("INSERT INTO core_payment_event_inbox(id,event_id,event_type,processed_time) VALUES (?,?,?,?)",ids.nextId(),e.eventId(),e.eventType(),LocalDateTime.now());}catch(DuplicateKeyException duplicate){return;}
    String orderNo=e.payload().path("orderNo").asText();long amount=e.payload().path("amountFen").asLong();
    if("payment.payment.succeeded".equals(e.eventType())){int n=jdbc.update("UPDATE charge_order SET payment_status=CASE WHEN paid_amount_fen+?>=receivable_amount_fen THEN 2 ELSE 1 END,trade_status=CASE WHEN paid_amount_fen+?>=receivable_amount_fen THEN 2 ELSE trade_status END,paid_time=CASE WHEN paid_amount_fen+?>=receivable_amount_fen THEN NOW(3) ELSE paid_time END,paid_amount_fen=LEAST(receivable_amount_fen,paid_amount_fen+?),version=version+1,update_time=NOW(3) WHERE tenant_id=? AND order_no=? AND paid_amount_fen+?<=receivable_amount_fen",amount,amount,amount,amount,e.tenantId(),orderNo,amount);if(n!=1)throw new IllegalStateException("order payment projection rejected: "+orderNo);}
    else if("payment.refund.succeeded".equals(e.eventType())){int n=jdbc.update("UPDATE charge_order SET refund_status=CASE WHEN refunded_amount_fen+?>=paid_amount_fen THEN 2 ELSE 1 END,refunded_amount_fen=refunded_amount_fen+?,version=version+1,update_time=NOW(3) WHERE tenant_id=? AND order_no=? AND refunded_amount_fen+?<=paid_amount_fen",amount,amount,e.tenantId(),orderNo,amount);if(n!=1)throw new IllegalStateException("refund projection rejected: "+orderNo);}
  }
}
