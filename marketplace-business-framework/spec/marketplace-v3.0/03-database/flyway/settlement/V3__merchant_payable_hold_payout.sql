CREATE TABLE merchant_settlement_hold (
  id BIGINT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  merchant_order_id BIGINT,
  order_item_id BIGINT,
  hold_type VARCHAR(32) NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_id VARCHAR(128) NOT NULL,
  currency CHAR(3),
  hold_amount DECIMAL(18,2),
  hold_until DATETIME(3),
  status VARCHAR(32) NOT NULL,
  reason VARCHAR(512),
  created_at DATETIME(3) NOT NULL,
  released_at DATETIME(3),
  UNIQUE KEY uk_settlement_hold_source (merchant_id, source_type, source_id, hold_type),
  KEY idx_settlement_hold_active (merchant_id, status, hold_until)
);

CREATE TABLE merchant_payable (
  id BIGINT PRIMARY KEY,
  payable_no VARCHAR(64) NOT NULL,
  merchant_id BIGINT NOT NULL,
  settlement_batch_id BIGINT NOT NULL,
  currency CHAR(3) NOT NULL,
  original_amount DECIMAL(18,2) NOT NULL,
  adjustment_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  payable_amount DECIMAL(18,2) NOT NULL,
  reserved_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  paid_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  outstanding_amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_payable_no (payable_no),
  UNIQUE KEY uk_merchant_payable_settlement (settlement_batch_id),
  KEY idx_merchant_payable (merchant_id, status, created_at)
);

CREATE TABLE payout_reservation (
  id BIGINT PRIMARY KEY,
  merchant_payable_id BIGINT NOT NULL,
  payout_order_id BIGINT NOT NULL,
  currency CHAR(3) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  released_at DATETIME(3),
  UNIQUE KEY uk_payout_reservation_order (payout_order_id),
  KEY idx_payout_reservation_payable (merchant_payable_id, status)
);

CREATE TABLE payout_transaction (
  id BIGINT PRIMARY KEY,
  payout_order_id BIGINT NOT NULL,
  provider_code VARCHAR(64) NOT NULL,
  provider_payout_no VARCHAR(128) NOT NULL,
  currency CHAR(3) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  provider_paid_at DATETIME(3),
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_provider_payout (provider_code, provider_payout_no),
  KEY idx_payout_tx_order (payout_order_id)
);

CREATE TABLE merchant_withdrawal_request (
  id BIGINT PRIMARY KEY,
  withdrawal_no VARCHAR(64) NOT NULL,
  merchant_id BIGINT NOT NULL,
  currency CHAR(3) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  provider_account_ref VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_withdrawal_no (withdrawal_no),
  UNIQUE KEY uk_withdrawal_idem (merchant_id, idempotency_key)
);
