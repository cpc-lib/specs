CREATE TABLE IF NOT EXISTS core_payment_event_inbox (
  id BIGINT NOT NULL,
  event_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(128) NOT NULL,
  processed_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_core_payment_event (event_id)
);
