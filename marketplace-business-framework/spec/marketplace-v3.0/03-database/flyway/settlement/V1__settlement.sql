CREATE TABLE merchant_balance_account (
 id BIGINT PRIMARY KEY,
 merchant_id BIGINT NOT NULL,
 currency CHAR(3) NOT NULL,
 pending_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 available_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 frozen_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 settling_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 negative_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 version BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_merchant_balance (merchant_id, currency)
);
CREATE TABLE settlement_batch (
 id BIGINT PRIMARY KEY,
 settlement_no VARCHAR(64) NOT NULL,
 merchant_id BIGINT NOT NULL,
 period_start DATETIME(3) NOT NULL,
 period_end DATETIME(3) NOT NULL,
 currency CHAR(3) NOT NULL,
 gross_amount DECIMAL(18,2) NOT NULL,
 commission_amount DECIMAL(18,2) NOT NULL,
 fee_amount DECIMAL(18,2) NOT NULL,
 adjustment_amount DECIMAL(18,2) NOT NULL,
 payable_amount DECIMAL(18,2) NOT NULL,
 status VARCHAR(32) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_settlement_no (settlement_no),
 KEY idx_settlement_merchant (merchant_id, status, period_end)
);
CREATE TABLE settlement_item (
 id BIGINT PRIMARY KEY,
 settlement_batch_id BIGINT NOT NULL,
 merchant_order_id BIGINT NOT NULL,
 order_item_id BIGINT,
 gross_amount DECIMAL(18,2) NOT NULL,
 commission_amount DECIMAL(18,2) NOT NULL,
 fee_amount DECIMAL(18,2) NOT NULL,
 refund_adjustment DECIMAL(18,2) NOT NULL DEFAULT 0,
 payable_amount DECIMAL(18,2) NOT NULL,
 calculation_snapshot_json JSON NOT NULL,
 UNIQUE KEY uk_settlement_item (settlement_batch_id, merchant_order_id, order_item_id)
);
CREATE TABLE payout_order (
 id BIGINT PRIMARY KEY,
 payout_no VARCHAR(64) NOT NULL,
 merchant_id BIGINT NOT NULL,
 settlement_batch_id BIGINT NOT NULL,
 currency CHAR(3) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 provider_code VARCHAR(64),
 provider_request_no VARCHAR(128),
 status VARCHAR(32) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_payout_no (payout_no),
 UNIQUE KEY uk_payout_provider_request (provider_code, provider_request_no)
);
