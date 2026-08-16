CREATE TABLE dispute_case (
 id BIGINT PRIMARY KEY,
 dispute_no VARCHAR(64) NOT NULL,
 aftersale_id BIGINT NOT NULL,
 buyer_id BIGINT NOT NULL,
 merchant_id BIGINT NOT NULL,
 status VARCHAR(32) NOT NULL,
 decision_type VARCHAR(32),
 decision_amount DECIMAL(18,2),
 decision_reason VARCHAR(2048),
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_dispute_no (dispute_no),
 KEY idx_dispute_status (status, created_at)
);
