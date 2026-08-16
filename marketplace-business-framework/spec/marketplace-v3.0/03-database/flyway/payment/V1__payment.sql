CREATE TABLE payment_order (
 id BIGINT PRIMARY KEY,
 payment_no VARCHAR(64) NOT NULL,
 buyer_id BIGINT NOT NULL,
 trade_id BIGINT NOT NULL,
 currency CHAR(3) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 status VARCHAR(32) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_payment_no (payment_no),
 KEY idx_payment_trade (trade_id)
);
CREATE TABLE payment_attempt (
 id BIGINT PRIMARY KEY,
 payment_order_id BIGINT NOT NULL,
 attempt_no VARCHAR(64) NOT NULL,
 channel VARCHAR(32) NOT NULL,
 provider_request_no VARCHAR(128) NOT NULL,
 status VARCHAR(32) NOT NULL,
 unknown_since DATETIME(3),
 expire_at DATETIME(3),
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_payment_attempt_no (attempt_no),
 UNIQUE KEY uk_payment_provider_request (channel, provider_request_no),
 KEY idx_payment_attempt_unknown (status, unknown_since)
);
CREATE TABLE payment_transaction (
 id BIGINT PRIMARY KEY,
 payment_order_id BIGINT NOT NULL,
 attempt_id BIGINT NOT NULL,
 channel VARCHAR(32) NOT NULL,
 channel_trade_no VARCHAR(128) NOT NULL,
 currency CHAR(3) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 transaction_status VARCHAR(32) NOT NULL,
 channel_paid_at DATETIME(3),
 raw_callback_ref VARCHAR(256),
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_payment_channel_trade (channel, channel_trade_no)
);
CREATE TABLE refund_order (
 id BIGINT PRIMARY KEY,
 refund_no VARCHAR(64) NOT NULL,
 payment_order_id BIGINT NOT NULL,
 trade_id BIGINT NOT NULL,
 merchant_order_id BIGINT,
 order_item_id BIGINT,
 currency CHAR(3) NOT NULL,
 refund_amount DECIMAL(18,2) NOT NULL,
 status VARCHAR(32) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_refund_no (refund_no)
);
CREATE TABLE refund_quota_reservation (
 id BIGINT PRIMARY KEY,
 payment_order_id BIGINT NOT NULL,
 refund_order_id BIGINT NOT NULL,
 reserved_amount DECIMAL(18,2) NOT NULL,
 status VARCHAR(32) NOT NULL,
 expire_at DATETIME(3),
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_refund_quota_order (refund_order_id),
 KEY idx_refund_quota_payment (payment_order_id, status)
);
