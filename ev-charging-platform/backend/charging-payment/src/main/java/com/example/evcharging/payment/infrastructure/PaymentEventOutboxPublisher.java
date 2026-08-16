package com.example.evcharging.payment.infrastructure;
import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.*;import java.util.*;
@Component
public class PaymentEventOutboxPublisher {
  private final JdbcTemplate jdbc;private final KafkaTemplate<String,String> kafka;private final ObjectMapper mapper;private final String topic;
  public PaymentEventOutboxPublisher(JdbcTemplate jdbc,KafkaTemplate<String,String> kafka,ObjectMapper mapper,@org.springframework.beans.factory.annotation.Value("${charging.payment.event-topic:ev.payment.payment.v1}") String topic){this.jdbc=jdbc;this.kafka=kafka;this.mapper=mapper;this.topic=topic;}
  @Scheduled(fixedDelayString="${charging.payment.outbox-delay-ms:500}")
  public void publish(){
    var rows=jdbc.query("SELECT id,event_id,aggregate_id,event_type,event_version,tenant_id,payload,occurred_time FROM payment_event_outbox WHERE status IN ('NEW','RETRY') AND (next_retry_time IS NULL OR next_retry_time<=NOW(3)) ORDER BY id LIMIT 100",(rs,n)->new Row(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getLong(6),rs.getString(7),rs.getObject(8,LocalDateTime.class)));
    for(Row r:rows)try{JsonNode payload=mapper.readTree(r.payload);var env=new DomainEventEnvelope<>(r.eventId,r.eventType,r.version,"Payment",r.aggregateId,r.tenantId,null,r.occurred.toInstant(ZoneOffset.UTC),"charging-payment",payload);kafka.send(topic,r.aggregateId,mapper.writeValueAsString(env)).get();jdbc.update("UPDATE payment_event_outbox SET status='PUBLISHED',published_time=NOW(3) WHERE id=?",r.id);}catch(Exception e){jdbc.update("UPDATE payment_event_outbox SET status='RETRY',retry_count=retry_count+1,next_retry_time=DATE_ADD(NOW(3),INTERVAL 5 SECOND) WHERE id=?",r.id);}
  }
  private record Row(long id,String eventId,String aggregateId,String eventType,String version,long tenantId,String payload,LocalDateTime occurred){}
}
