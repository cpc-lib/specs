CREATE TABLE mq_outbox (
 id BIGINT PRIMARY KEY,
 event_id VARCHAR(64) NOT NULL,
 event_type VARCHAR(128) NOT NULL,
 aggregate_type VARCHAR(64) NOT NULL,
 aggregate_id VARCHAR(128) NOT NULL,
 payload_json JSON NOT NULL,
 status VARCHAR(32) NOT NULL,
 retry_count INT NOT NULL DEFAULT 0,
 next_retry_at DATETIME(3),
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_outbox_event (event_id),
 KEY idx_outbox_pending (status, next_retry_at, id)
);
CREATE TABLE mq_inbox (
 id BIGINT PRIMARY KEY,
 consumer_group VARCHAR(128) NOT NULL,
 event_id VARCHAR(64) NOT NULL,
 processed_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_inbox_consumer_event (consumer_group, event_id)
);
CREATE TABLE integration_task (
 id BIGINT PRIMARY KEY,
 task_type VARCHAR(64) NOT NULL,
 business_type VARCHAR(64) NOT NULL,
 business_id VARCHAR(128) NOT NULL,
 status VARCHAR(32) NOT NULL,
 retry_count INT NOT NULL DEFAULT 0,
 last_error VARCHAR(2000),
 next_retry_at DATETIME(3),
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 KEY idx_integration_task (status, next_retry_at)
);
