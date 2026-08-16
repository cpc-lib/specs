package com.example.evcharging.core.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AssetStationEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(AssetStationEventConsumer.class);
    private static final String CONSUMER = "charging-core-station-projection";

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public AssetStationEventConsumer(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "ev.asset.station.v1", groupId = "charging-core-asset-projection")
    @Transactional
    public void onEvent(String raw) throws Exception {
        JsonNode envelope = mapper.readTree(raw);
        String eventId = envelope.path("eventId").asText();
        String eventType = envelope.path("eventType").asText();
        long tenantId = envelope.path("tenantId").asLong();
        if (eventId.isBlank() || eventType.isBlank() || tenantId <= 0) {
            throw new IllegalArgumentException("invalid event envelope");
        }
        if ("asset.station.created".equals(eventType)) {
            JsonNode payload = envelope.path("payload");
            if (payload.path("stationId").asLong() <= 0
                    || payload.path("stationCode").asText().isBlank()
                    || payload.path("stationName").asText().isBlank()) {
                throw new IllegalArgumentException("invalid station event payload");
            }
        }

        int inserted = jdbc.update(
                "INSERT IGNORE INTO event_inbox(consumer_name,event_id,event_type,processed_time) VALUES (?,?,?,?)",
                CONSUMER, eventId, eventType, LocalDateTime.now());
        if (inserted == 0) {
            log.debug("duplicate event ignored: {}", eventId);
            return;
        }

        if ("asset.station.created".equals(eventType)) {
            JsonNode payload = envelope.path("payload");
            jdbc.update(
                    "INSERT INTO station_projection(tenant_id,station_id,station_code,station_name,update_time) VALUES (?,?,?,?,?) "
                            + "ON DUPLICATE KEY UPDATE station_code=VALUES(station_code),station_name=VALUES(station_name),update_time=VALUES(update_time)",
                    tenantId,
                    payload.path("stationId").asLong(),
                    payload.path("stationCode").asText(),
                    payload.path("stationName").asText(),
                    LocalDateTime.now());
        }
    }
}
