ALTER TABLE event_outbox
    ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0 AFTER id,
    ADD COLUMN trace_id VARCHAR(128) NULL AFTER payload,
    ADD COLUMN locked_by VARCHAR(128) NULL AFTER next_retry_time,
    ADD COLUMN locked_until DATETIME(3) NULL AFTER locked_by,
    ADD COLUMN last_error VARCHAR(1000) NULL AFTER locked_until;

CREATE INDEX idx_outbox_lock
    ON event_outbox(status, next_retry_time, locked_until);
