package com.example.evcharging.asset;

import com.example.evcharging.asset.station.CreateStationRequest;
import com.example.evcharging.asset.station.StationApplicationService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "outbox.publisher.enabled=false"
})
class StationApplicationIntegrationTest {
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

    @Autowired StationApplicationService service;
    @Autowired JdbcTemplate jdbc;

    @Test
    void stationAndOutboxShouldCommitTogether() {
        var station = service.create(1L, new CreateStationRequest(1L, "ST001", "Test Station"));
        Integer stationCount = jdbc.queryForObject("SELECT COUNT(*) FROM station WHERE id=?", Integer.class, station.getId());
        Integer outboxCount = jdbc.queryForObject("SELECT COUNT(*) FROM event_outbox WHERE aggregate_id=?", Integer.class, String.valueOf(station.getId()));
        assertThat(stationCount).isEqualTo(1);
        assertThat(outboxCount).isEqualTo(1);
    }

    @Test
    void duplicateStationMustNotCreateSecondOutbox() {
        service.create(2L, new CreateStationRequest(1L, "DUP001", "First"));
        assertThatThrownBy(() -> service.create(2L, new CreateStationRequest(1L, "DUP001", "Duplicate")))
                .isInstanceOf(RuntimeException.class);
        Integer stations = jdbc.queryForObject("SELECT COUNT(*) FROM station WHERE tenant_id=2 AND station_code='DUP001'", Integer.class);
        Integer outbox = jdbc.queryForObject("SELECT COUNT(*) FROM event_outbox WHERE tenant_id=2 AND event_type='asset.station.created'", Integer.class);
        assertThat(stations).isEqualTo(1);
        assertThat(outbox).isEqualTo(1);
    }

    @Test
    void listMustBeTenantScoped() {
        service.create(10L, new CreateStationRequest(1L, "TENANT10", "Tenant 10"));
        service.create(11L, new CreateStationRequest(1L, "TENANT11", "Tenant 11"));
        assertThat(service.list(10L)).extracting("stationCode").contains("TENANT10").doesNotContain("TENANT11");
    }
}
