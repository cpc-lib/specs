package com.example.evcharging.core.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class AssetStationEventConsumerIntegrationTest {
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0.36")
            .withDatabaseName("ev_charging_platform")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired AssetStationEventConsumer consumer;
    @Autowired JdbcTemplate jdbc;

    @Test
    void duplicateEventMustHaveOneLogicalEffect() throws Exception {
        String event = """
                {
                  "eventId":"EV-001",
                  "eventType":"asset.station.created",
                  "eventVersion":"1.0",
                  "aggregateType":"Station",
                  "aggregateId":"100",
                  "tenantId":1,
                  "traceId":"REQ-1",
                  "occurredAt":"2026-08-10T01:00:00Z",
                  "producer":"charging-asset",
                  "payload":{
                    "stationId":100,
                    "stationCode":"ST100",
                    "stationName":"Station 100"
                  }
                }
                """;

        consumer.onEvent(event);
        consumer.onEvent(event);

        Integer inbox = jdbc.queryForObject("SELECT COUNT(*) FROM event_inbox WHERE event_id='EV-001'", Integer.class);
        Integer projection = jdbc.queryForObject("SELECT COUNT(*) FROM station_projection WHERE tenant_id=1 AND station_id=100", Integer.class);
        assertThat(inbox).isEqualTo(1);
        assertThat(projection).isEqualTo(1);
    }
}
