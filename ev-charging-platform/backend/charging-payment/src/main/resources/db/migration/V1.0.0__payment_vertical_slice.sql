CREATE TABLE IF NOT EXISTS payment_order (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  request_id VARCHAR(128) NOT NULL,
  biz_type VARCHAR(32) NOT NULL,
  biz_order_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  channel VARCHAR(32) NOT NULL,
  amount_fen BIGINT NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'CNY',
  status VARCHAR(32) NOT NULL,
  channel_trade_no VARCHAR(128),
  payment_token VARCHAR(512),
  refunded_amount_fen BIGINT NOT NULL DEFAULT 0,
  refund_reserved_fen BIGINT NOT NULL DEFAULT 0,
  success_time DATETIME(3),
  version INT NOT NULL DEFAULT 0,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_no (payment_no),
  UNIQUE KEY uk_tenant_request (tenant_id, request_id),
  KEY idx_payment_biz_order (tenant_id, biz_type, biz_order_no),
  KEY idx_payment_status_time (status, update_time),
  KEY idx_channel_trade (channel, channel_trade_no)
);

CREATE TABLE IF NOT EXISTS payment_transaction (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  payment_id BIGINT NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  transaction_type VARCHAR(32) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  channel_trade_no VARCHAR(128),
  amount_fen BIGINT NOT NULL,
  channel_status VARCHAR(32) NOT NULL,
  occurred_time DATETIME(3) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_payment_tx_payment (payment_id, occurred_time),
  KEY idx_payment_tx_channel (channel, channel_trade_no)
);

CREATE TABLE IF NOT EXISTS payment_callback_log (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  callback_fingerprint VARCHAR(128) NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  callback_status VARCHAR(32) NOT NULL,
  raw_payload TEXT,
  processed_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_callback_fingerprint (tenant_id, callback_fingerprint)
);

CREATE TABLE IF NOT EXISTS payment_refund (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  refund_no VARCHAR(64) NOT NULL,
  request_id VARCHAR(128) NOT NULL,
  payment_id BIGINT NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  amount_fen BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  channel_refund_no VARCHAR(128),
  success_time DATETIME(3),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refund_no (refund_no),
  UNIQUE KEY uk_refund_request (tenant_id, request_id),
  KEY idx_refund_payment (payment_id, create_time)
);

CREATE TABLE IF NOT EXISTS payment_event_outbox (
  id BIGINT NOT NULL,
  event_id VARCHAR(64) NOT NULL,
  aggregate_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(128) NOT NULL,
  event_version VARCHAR(16) NOT NULL,
  tenant_id BIGINT NOT NULL,
  payload JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_time DATETIME(3),
  occurred_time DATETIME(3) NOT NULL,
  published_time DATETIME(3),
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_event (event_id),
  KEY idx_payment_outbox_publish (status, next_retry_time)
);

CREATE TABLE IF NOT EXISTS payment_active_order (
  tenant_id BIGINT NOT NULL,
  biz_order_no VARCHAR(64) NOT NULL,
  payment_id BIGINT NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (tenant_id, biz_order_no),
  UNIQUE KEY uk_active_payment (payment_id),
  UNIQUE KEY uk_active_payment_no (payment_no)
);
