CREATE TABLE reconciliation_batch (
 id BIGINT PRIMARY KEY,
 batch_no VARCHAR(64) NOT NULL,
 reconciliation_type VARCHAR(32) NOT NULL,
 provider_code VARCHAR(64),
 statement_date DATE,
 status VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_recon_batch_no (batch_no)
);
CREATE TABLE reconciliation_item (
 id BIGINT PRIMARY KEY,
 batch_id BIGINT NOT NULL,
 business_key VARCHAR(128) NOT NULL,
 local_amount DECIMAL(18,2),
 external_amount DECIMAL(18,2),
 result VARCHAR(32) NOT NULL,
 severity VARCHAR(32) NOT NULL,
 detail_json JSON,
 KEY idx_recon_item_batch (batch_id, result, severity)
);
