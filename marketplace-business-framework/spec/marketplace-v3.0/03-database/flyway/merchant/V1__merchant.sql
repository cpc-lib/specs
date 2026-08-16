CREATE TABLE merchant (
 id BIGINT PRIMARY KEY,
 merchant_no VARCHAR(64) NOT NULL,
 merchant_type VARCHAR(32) NOT NULL,
 legal_name VARCHAR(256) NOT NULL,
 status VARCHAR(32) NOT NULL,
 risk_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_merchant_no (merchant_no)
);
CREATE TABLE merchant_membership (
 id BIGINT PRIMARY KEY,
 merchant_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 status VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_merchant_membership (merchant_id, user_id),
 KEY idx_membership_user (user_id, status)
);
CREATE TABLE merchant_deposit_account (
 id BIGINT PRIMARY KEY,
 merchant_id BIGINT NOT NULL,
 currency CHAR(3) NOT NULL,
 required_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 received_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 frozen_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 deducted_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 refundable_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 status VARCHAR(32) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_merchant_deposit (merchant_id)
);
