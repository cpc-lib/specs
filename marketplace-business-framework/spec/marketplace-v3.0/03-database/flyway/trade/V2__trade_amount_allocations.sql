CREATE TABLE discount_allocation (
 id BIGINT PRIMARY KEY,
 trade_id BIGINT NOT NULL,
 merchant_order_id BIGINT NOT NULL,
 order_item_id BIGINT NOT NULL,
 discount_source_type VARCHAR(32) NOT NULL,
 discount_source_id BIGINT NULL,
 funding_party_type VARCHAR(32) NOT NULL,
 funding_merchant_id BIGINT NULL,
 original_discount_amount DECIMAL(18,2) NOT NULL,
 allocated_discount_amount DECIMAL(18,2) NOT NULL,
 rounding_residual DECIMAL(18,2) NOT NULL DEFAULT 0,
 calculation_trace_json JSON NOT NULL,
 created_at DATETIME(3) NOT NULL,
 KEY idx_discount_trade (trade_id, discount_source_type, discount_source_id),
 KEY idx_discount_item (order_item_id)
);

CREATE TABLE order_address_snapshot (
 id BIGINT PRIMARY KEY,
 trade_id BIGINT NOT NULL,
 buyer_id BIGINT NOT NULL,
 consignee VARCHAR(128) NOT NULL,
 phone_masked VARCHAR(64) NOT NULL,
 province VARCHAR(64) NOT NULL,
 city VARCHAR(64) NOT NULL,
 district VARCHAR(64) NOT NULL,
 street VARCHAR(128) NULL,
 detail_address VARCHAR(512) NOT NULL,
 longitude DECIMAL(10,7) NULL,
 latitude DECIMAL(10,7) NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_trade_address_snapshot (trade_id)
);

CREATE TABLE trade_idempotency (
 id BIGINT PRIMARY KEY,
 buyer_id BIGINT NOT NULL,
 checkout_token VARCHAR(128) NOT NULL,
 idempotency_key VARCHAR(128) NOT NULL,
 request_hash VARCHAR(128) NOT NULL,
 trade_id BIGINT NULL,
 result_json JSON NULL,
 status VARCHAR(32) NOT NULL,
 expire_at DATETIME(3) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_trade_idempotency (buyer_id, idempotency_key),
 UNIQUE KEY uk_trade_checkout_token (buyer_id, checkout_token),
 KEY idx_trade_idem_expire (status, expire_at)
);
