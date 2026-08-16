CREATE TABLE commission_rule (
 id BIGINT PRIMARY KEY,
 rule_no VARCHAR(64) NOT NULL,
 merchant_id BIGINT NULL,
 shop_id BIGINT NULL,
 category_id BIGINT NULL,
 rule_type VARCHAR(32) NOT NULL,
 rate DECIMAL(12,8) NULL,
 fixed_amount DECIMAL(18,2) NULL,
 effective_from DATETIME(3) NOT NULL,
 effective_to DATETIME(3) NULL,
 version_no INT NOT NULL,
 status VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_commission_rule_version (rule_no, version_no),
 KEY idx_commission_effective (merchant_id, shop_id, category_id, status, effective_from, effective_to)
);

CREATE TABLE settlement_eligibility (
 id BIGINT PRIMARY KEY,
 merchant_id BIGINT NOT NULL,
 merchant_order_id BIGINT NOT NULL,
 order_item_id BIGINT NULL,
 currency CHAR(3) NOT NULL,
 eligible_gross_amount DECIMAL(18,2) NOT NULL,
 completed_at DATETIME(3) NOT NULL,
 hold_until DATETIME(3) NOT NULL,
 status VARCHAR(32) NOT NULL,
 blocking_reason VARCHAR(64) NULL,
 risk_hold_ref VARCHAR(128) NULL,
 consumed_settlement_batch_id BIGINT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_settlement_eligibility_item (merchant_order_id, order_item_id),
 KEY idx_settlement_eligibility_scan (merchant_id, status, hold_until, id)
);

CREATE TABLE settlement_adjustment (
 id BIGINT PRIMARY KEY,
 adjustment_no VARCHAR(64) NOT NULL,
 merchant_id BIGINT NOT NULL,
 original_settlement_batch_id BIGINT NULL,
 source_type VARCHAR(32) NOT NULL,
 source_id BIGINT NOT NULL,
 currency CHAR(3) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 reason_code VARCHAR(64) NOT NULL,
 reason VARCHAR(1024) NULL,
 status VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_settlement_adjustment_no (adjustment_no),
 KEY idx_settlement_adjustment_merchant (merchant_id, status, created_at)
);

CREATE TABLE merchant_balance_ledger (
 id BIGINT PRIMARY KEY,
 merchant_id BIGINT NOT NULL,
 currency CHAR(3) NOT NULL,
 bucket VARCHAR(32) NOT NULL,
 direction VARCHAR(8) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 business_type VARCHAR(32) NOT NULL,
 business_id VARCHAR(128) NOT NULL,
 balance_after DECIMAL(18,2) NOT NULL,
 occurred_at DATETIME(3) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_merchant_balance_fact (merchant_id, currency, bucket, business_type, business_id),
 KEY idx_merchant_balance_ledger (merchant_id, currency, occurred_at)
);
