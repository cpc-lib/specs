package com.example.evcharging.core.charging.infrastructure;

import com.example.evcharging.framework.contract.DeviceCommandMessage;
import com.example.evcharging.framework.contract.DeviceRouteLease;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DeviceCommandOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(DeviceCommandOutboxPublisher.class);
    private static final int NEW = 0;
    private static final int PUBLISHED = 1;
    private static final int DEAD = 3;
    private static final int PUBLISHING = 9;

    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final String exchange;
    private final String routingKeyPrefix;
    private final int maxRetries;

    public DeviceCommandOutboxPublisher(JdbcTemplate jdbc, RabbitTemplate rabbit, StringRedisTemplate redis, ObjectMapper mapper,
            @Value("${charging.command-outbox.exchange:ev.device.command.exchange}") String exchange,
            @Value("${charging.command-outbox.routing-key-prefix:gateway.}") String routingKeyPrefix,
            @Value("${charging.command-outbox.max-retries:5}") int maxRetries) {
        this.jdbc=jdbc; this.rabbit=rabbit; this.redis=redis; this.mapper=mapper; this.exchange=exchange; this.routingKeyPrefix=routingKeyPrefix; this.maxRetries=maxRetries;
    }

    @Scheduled(fixedDelayString="${charging.command-outbox.delay-ms:500}")
    public void publish() {
        jdbc.update("UPDATE device_command_outbox SET status=?,last_error='stale publishing claim recovered',update_time=NOW(3) WHERE status=? AND update_time<DATE_SUB(NOW(3),INTERVAL 30 SECOND) AND expire_time>NOW(3)", NEW, PUBLISHING);
        jdbc.update("UPDATE device_command_outbox SET status=?,last_error='command expired',update_time=NOW(3) WHERE status IN (?,?) AND expire_time<=NOW(3)", DEAD, NEW, PUBLISHING);
        var rows = jdbc.queryForList("SELECT id,tenant_id,command_id,device_id,connector_no,command_type,payload,expire_time,retry_count FROM device_command_outbox WHERE status=? AND expire_time>NOW(3) ORDER BY id LIMIT 100", NEW);
        for (Map<String,Object> row : rows) publishOne(row);
    }

    private void publishOne(Map<String,Object> row) {
        long id=((Number)row.get("id")).longValue();
        if (jdbc.update("UPDATE device_command_outbox SET status=?,update_time=NOW(3) WHERE id=? AND status=?", PUBLISHING, id, NEW) != 1) return;
        try {
            @SuppressWarnings("unchecked") Map<String,Object> payload=mapper.readValue(String.valueOf(row.get("payload")),Map.class);
            var cmd=new DeviceCommandMessage(
                    ((Number)row.get("tenant_id")).longValue(), String.valueOf(row.get("command_id")), String.valueOf(row.get("device_id")),
                    String.valueOf(row.get("command_type")), ((java.sql.Timestamp)row.get("expire_time")).toInstant(), payload);
            String routeValue = redis.opsForValue().get("ev:" + cmd.tenantId() + ":device:route:" + cmd.deviceId());
            if (routeValue == null || routeValue.isBlank()) {
                throw new IllegalStateException("device route unavailable: " + cmd.deviceId());
            }
            DeviceRouteLease route = DeviceRouteLease.parse(routeValue);
            CorrelationData correlation = new CorrelationData(cmd.commandId());
            rabbit.convertAndSend(exchange, routingKeyPrefix + route.gatewayId(), mapper.writeValueAsString(cmd), correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.isAck()) throw new IllegalStateException("RabbitMQ nack: " + confirm.getReason());
            if (correlation.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ returned unroutable command: " + correlation.getReturned().getReplyText());
            }
            jdbc.update("UPDATE device_command_outbox SET status=?,last_error=NULL,update_time=NOW(3) WHERE id=? AND status=?", PUBLISHED, id, PUBLISHING);
        } catch (Exception e) {
            int retry=((Number)row.get("retry_count")).intValue()+1;
            int next=retry>=maxRetries?DEAD:NEW;
            jdbc.update("UPDATE device_command_outbox SET status=?,retry_count=?,last_error=?,update_time=NOW(3) WHERE id=? AND status=?",
                    next, retry, abbreviate(e.toString(), 500), id, PUBLISHING);
            log.warn("device command publish failed id={} retry={} nextStatus={}", id, retry, next, e);
        }
    }

    private static String abbreviate(String value,int max){return value==null?null:(value.length()<=max?value:value.substring(0,max));}
}
