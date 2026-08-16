package com.example.evcharging.asset.event;

import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "outbox.publisher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final String TOPIC = "ev.asset.station.v1";

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String workerId = UUID.randomUUID().toString();

    public OutboxPublisher(OutboxRepository repository, KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.repository = repository;
        this.kafka = kafka;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-delay-ms:1000}")
    public void publish() {
        List<Map<String, Object>> rows = repository.claimBatch(workerId, 100);
        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("id")).longValue();
            try {
                JsonNode payload = mapper.readTree(String.valueOf(row.get("payload")));
                Timestamp occurred = (Timestamp) row.get("occurred_time");
                var envelope = new DomainEventEnvelope<>(
                        String.valueOf(row.get("event_id")),
                        String.valueOf(row.get("event_type")),
                        String.valueOf(row.get("event_version")),
                        String.valueOf(row.get("aggregate_type")),
                        String.valueOf(row.get("aggregate_id")),
                        ((Number) row.get("tenant_id")).longValue(),
                        row.get("trace_id") == null ? null : String.valueOf(row.get("trace_id")),
                        occurred == null ? Instant.now() : occurred.toInstant(),
                        "charging-asset",
                        payload
                );

                kafka.send(TOPIC, String.valueOf(row.get("aggregate_id")), mapper.writeValueAsString(envelope))
                        .get(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
                repository.markPublished(id);
            } catch (Exception e) {
                int currentRetry = repository.retryCount(id);
                repository.markFailed(id, currentRetry, e.toString());
                log.warn("outbox publish failed: id={}, retry={}", id, currentRetry + 1, e);
            }
        }
    }
}
