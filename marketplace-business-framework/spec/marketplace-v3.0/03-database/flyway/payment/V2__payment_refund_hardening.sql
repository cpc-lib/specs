CREATE TABLE trade_payment_allocation (
 id BIGINT PRIMARY KEY,
 payment_transaction_id BIGINT NOT NULL,
 payment_no VARCHAR(64) NOT NULL,
 trade_id BIGINT NOT NULL,
 merchant_order_id BIGINT NOT NULL,
 order_item_id BIGINT NULL,
 allocation_type VARCHAR(32) NOT NULL,
 currency CHAR(3) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 KEY idx_tpa_transaction (payment_transaction_id),
 KEY idx_tpa_trade (trade_id, merchant_order_id),
 UNIQUE KEY uk_tpa_fact (payment_transaction_id, merchant_order_id, order_item_id, allocation_type)
);

CREATE TABLE payment_callback_log (
 id BIGINT PRIMARY KEY,
 channel VARCHAR(32) NOT NULL,
 callback_key VARCHAR(256) NOT NULL,
 payment_no VARCHAR(64) NULL,
 provider_trade_no VARCHAR(128) NULL,
 signature_valid TINYINT NOT NULL,
 amount_valid TINYINT NULL,
 processing_result VARCHAR(32) NOT NULL,
 raw_payload_ref VARCHAR(512) NULL,
 received_at DATETIME(3) NOT NULL,
 processed_at DATETIME(3) NULL,
 UNIQUE KEY uk_payment_callback_key (channel, callback_key),
 KEY idx_callback_payment (payment_no, received_at)
);

CREATE TABLE refund_transaction (
 id BIGINT PRIMARY KEY,
 refund_order_id BIGINT NOT NULL,
 refund_no VARCHAR(64) NOT NULL,
 channel VARCHAR(32) NOT NULL,
 channel_refund_no VARCHAR(128) NOT NULL,
 currency CHAR(3) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 transaction_status VARCHAR(32) NOT NULL,
 channel_refunded_at DATETIME(3) NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_refund_channel_no (channel, channel_refund_no),
 KEY idx_refund_tx_order (refund_order_id)
);
