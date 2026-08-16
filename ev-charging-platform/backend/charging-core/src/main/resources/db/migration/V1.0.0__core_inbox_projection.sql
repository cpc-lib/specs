CREATE TABLE event_inbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    consumer_name VARCHAR(128) NOT NULL,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    processed_time DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_consumer_event (consumer_name, event_id)
);

CREATE TABLE station_projection (
    tenant_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    station_code VARCHAR(64) NOT NULL,
    station_name VARCHAR(128) NOT NULL,
    update_time DATETIME(3) NOT NULL,
    PRIMARY KEY (tenant_id, station_id),
    KEY idx_projection_code (tenant_id, station_code)
);
