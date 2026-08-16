CREATE TABLE payment_clearing_record (
  id BIGINT PRIMARY KEY,
  clearing_no VARCHAR(64) NOT NULL,
  payment_transaction_id BIGINT NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  trade_id BIGINT NOT NULL,
  channel VARCHAR(32) NOT NULL,
  provider_merchant_ref VARCHAR(128),
  currency CHAR(3) NOT NULL,
  gross_received_amount DECIMAL(18,2) NOT NULL,
  provider_fee_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  clearing_amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_payment_clearing_no (clearing_no),
  UNIQUE KEY uk_payment_clearing_tx (payment_transaction_id),
  KEY idx_payment_clearing_trade (trade_id, status)
);

CREATE TABLE payment_clearing_allocation (
  id BIGINT PRIMARY KEY,
  clearing_record_id BIGINT NOT NULL,
  merchant_id BIGINT,
  shop_id BIGINT,
  merchant_order_id BIGINT,
  order_item_id BIGINT,
  allocation_type VARCHAR(48) NOT NULL,
  currency CHAR(3) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  source_allocation_id BIGINT,
  created_at DATETIME(3) NOT NULL,
  KEY idx_clearing_alloc_record (clearing_record_id),
  KEY idx_clearing_alloc_merchant (merchant_id, merchant_order_id)
);

CREATE TABLE refund_reverse_allocation (
  id BIGINT PRIMARY KEY,
  refund_no VARCHAR(64) NOT NULL,
  refund_transaction_id BIGINT NOT NULL,
  merchant_id BIGINT,
  merchant_order_id BIGINT,
  order_item_id BIGINT,
  original_funding_allocation_id BIGINT,
  reverse_type VARCHAR(48) NOT NULL,
  currency CHAR(3) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  settlement_impact VARCHAR(48) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_refund_reverse_refund (refund_no),
  KEY idx_refund_reverse_item (order_item_id, reverse_type)
);

CREATE TABLE finance_daily_close (
  id BIGINT PRIMARY KEY,
  close_date DATE NOT NULL,
  currency CHAR(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  critical_exception_count INT NOT NULL DEFAULT 0,
  high_exception_count INT NOT NULL DEFAULT 0,
  summary_json JSON NOT NULL,
  started_at DATETIME(3),
  completed_at DATETIME(3),
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_finance_daily_close (close_date, currency)
);

CREATE TABLE finance_control_check (
  id BIGINT PRIMARY KEY,
  daily_close_id BIGINT NOT NULL,
  check_code VARCHAR(64) NOT NULL,
  business_key VARCHAR(128),
  severity VARCHAR(32) NOT NULL,
  result VARCHAR(32) NOT NULL,
  expected_amount DECIMAL(18,2),
  actual_amount DECIMAL(18,2),
  detail_json JSON,
  created_at DATETIME(3) NOT NULL,
  KEY idx_control_close (daily_close_id, result, severity),
  KEY idx_control_business (check_code, business_key)
);
