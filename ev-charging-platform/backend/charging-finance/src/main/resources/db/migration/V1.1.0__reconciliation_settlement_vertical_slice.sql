ALTER TABLE finance_event_inbox
  ADD COLUMN consumer_name VARCHAR(64) NOT NULL DEFAULT 'ledger-v1' AFTER id,
  DROP INDEX uk_finance_event,
  ADD UNIQUE KEY uk_finance_consumer_event (consumer_name, event_id);

CREATE TABLE IF NOT EXISTS finance_transaction_fact (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, payment_no VARCHAR(64) NOT NULL,
  biz_order_no VARCHAR(64) NOT NULL, channel VARCHAR(32) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL DEFAULT 'DEFAULT', channel_trade_no VARCHAR(128),
  amount_fen BIGINT NOT NULL, currency CHAR(3) NOT NULL DEFAULT 'CNY', payment_status VARCHAR(32) NOT NULL,
  success_time DATETIME(3) NOT NULL, business_date DATE NOT NULL, source_event_id VARCHAR(64) NOT NULL,
  create_time DATETIME(3) NOT NULL, update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_finance_payment_fact (tenant_id, payment_no),
  UNIQUE KEY uk_finance_payment_event (tenant_id, source_event_id),
  KEY idx_finance_payment_date (tenant_id, channel, merchant_id, business_date),
  UNIQUE KEY uk_finance_channel_trade (tenant_id, channel, channel_trade_no)
);
CREATE TABLE IF NOT EXISTS finance_refund_fact (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, refund_no VARCHAR(64) NOT NULL, payment_no VARCHAR(64) NOT NULL,
  amount_fen BIGINT NOT NULL, refund_status VARCHAR(32) NOT NULL, success_time DATETIME(3) NOT NULL,
  business_date DATE NOT NULL, source_event_id VARCHAR(64) NOT NULL, create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_finance_refund_fact (tenant_id, refund_no),
  UNIQUE KEY uk_finance_refund_event (tenant_id, source_event_id), KEY idx_finance_refund_payment (tenant_id,payment_no,success_time)
);
CREATE TABLE IF NOT EXISTS finance_channel_bill_batch (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, batch_no VARCHAR(64) NOT NULL, channel VARCHAR(32) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL, business_date DATE NOT NULL, source_file_name VARCHAR(255), source_file_hash VARCHAR(128) NOT NULL,
  record_count INT NOT NULL, total_amount_fen BIGINT NOT NULL, total_refund_fen BIGINT NOT NULL, status VARCHAR(32) NOT NULL,
  create_time DATETIME(3) NOT NULL, PRIMARY KEY(id), UNIQUE KEY uk_channel_bill_no(batch_no),
  UNIQUE KEY uk_channel_bill_hash(tenant_id,channel,merchant_id,source_file_hash), KEY idx_channel_bill_date(tenant_id,channel,merchant_id,business_date)
);
CREATE TABLE IF NOT EXISTS finance_channel_transaction (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, batch_id BIGINT NOT NULL, channel VARCHAR(32) NOT NULL, merchant_id VARCHAR(64) NOT NULL,
  business_date DATE NOT NULL, payment_no VARCHAR(64), channel_trade_no VARCHAR(128) NOT NULL, amount_fen BIGINT NOT NULL,
  refund_amount_fen BIGINT NOT NULL DEFAULT 0, channel_status VARCHAR(32) NOT NULL, occurred_time DATETIME(3) NOT NULL,
  raw_payload JSON, create_time DATETIME(3) NOT NULL, PRIMARY KEY(id),
  UNIQUE KEY uk_channel_transaction(tenant_id,channel,merchant_id,channel_trade_no), UNIQUE KEY uk_channel_payment(tenant_id,channel,merchant_id,payment_no), KEY idx_channel_tx_date(tenant_id,channel,merchant_id,business_date),
  KEY idx_channel_tx_payment(tenant_id,payment_no)
);
CREATE TABLE IF NOT EXISTS finance_reconciliation_batch (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, batch_no VARCHAR(64) NOT NULL, request_id VARCHAR(128) NOT NULL,
  channel VARCHAR(32) NOT NULL, merchant_id VARCHAR(64) NOT NULL, business_date DATE NOT NULL, status VARCHAR(32) NOT NULL,
  local_count INT NOT NULL DEFAULT 0, channel_count INT NOT NULL DEFAULT 0, match_count INT NOT NULL DEFAULT 0,
  difference_count INT NOT NULL DEFAULT 0, started_time DATETIME(3) NOT NULL, completed_time DATETIME(3), create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_reconciliation_batch_no(batch_no), UNIQUE KEY uk_reconciliation_request(tenant_id,request_id),
  KEY idx_reconciliation_date(tenant_id,channel,merchant_id,business_date)
);
CREATE TABLE IF NOT EXISTS finance_reconciliation_detail (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, batch_id BIGINT NOT NULL, payment_no VARCHAR(64), channel_trade_no VARCHAR(128),
  local_amount_fen BIGINT, channel_amount_fen BIGINT, local_refund_fen BIGINT, channel_refund_fen BIGINT,
  local_status VARCHAR(32), channel_status VARCHAR(32), result_type VARCHAR(32) NOT NULL, difference_amount_fen BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME(3) NOT NULL, PRIMARY KEY(id), KEY idx_reconciliation_result(batch_id,result_type),
  KEY idx_reconciliation_payment(tenant_id,payment_no), KEY idx_reconciliation_trade(tenant_id,channel_trade_no)
);
CREATE TABLE IF NOT EXISTS finance_difference_case (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, case_no VARCHAR(64) NOT NULL, reconciliation_batch_id BIGINT NOT NULL,
  reconciliation_detail_id BIGINT NOT NULL, difference_type VARCHAR(32) NOT NULL, status VARCHAR(32) NOT NULL, reason VARCHAR(255),
  resolution VARCHAR(512), resolved_by BIGINT, resolved_time DATETIME(3), create_time DATETIME(3) NOT NULL, update_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_difference_case_no(case_no), UNIQUE KEY uk_difference_detail(reconciliation_detail_id),
  KEY idx_difference_status(tenant_id,status,create_time)
);
CREATE TABLE IF NOT EXISTS finance_settlement_source (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, source_no VARCHAR(64) NOT NULL, reconciliation_detail_id BIGINT NOT NULL,
  payment_no VARCHAR(64) NOT NULL, biz_order_no VARCHAR(64) NOT NULL, channel VARCHAR(32) NOT NULL, merchant_id VARCHAR(64) NOT NULL,
  business_date DATE NOT NULL, gross_amount_fen BIGINT NOT NULL, refund_amount_fen BIGINT NOT NULL, settlement_base_amount_fen BIGINT NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'CNY', status VARCHAR(32) NOT NULL, create_time DATETIME(3) NOT NULL, update_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_settlement_source_no(source_no), UNIQUE KEY uk_settlement_payment(tenant_id,payment_no),
  KEY idx_settlement_source_ready(tenant_id,business_date,status)
);
CREATE TABLE IF NOT EXISTS finance_settlement_rule (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, rule_code VARCHAR(64) NOT NULL, rule_name VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL, create_time DATETIME(3) NOT NULL, update_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_settlement_rule_code(tenant_id,rule_code)
);
CREATE TABLE IF NOT EXISTS finance_settlement_rule_version (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, rule_id BIGINT NOT NULL, version_no INT NOT NULL, status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL, effective_to DATETIME(3), published_time DATETIME(3), create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_settlement_rule_version(rule_id,version_no), KEY idx_settlement_rule_effective(tenant_id,status,effective_from,effective_to)
);
CREATE TABLE IF NOT EXISTS finance_settlement_rule_item (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, version_id BIGINT NOT NULL, participant_type VARCHAR(32) NOT NULL,
  participant_id VARCHAR(64) NOT NULL, calculation_type VARCHAR(32) NOT NULL, ratio_bps INT, fixed_amount_fen BIGINT,
  priority_no INT NOT NULL DEFAULT 0, create_time DATETIME(3) NOT NULL, PRIMARY KEY(id),
  UNIQUE KEY uk_settlement_rule_participant(version_id,participant_type,participant_id)
);
CREATE TABLE IF NOT EXISTS finance_settlement_batch (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, batch_no VARCHAR(64) NOT NULL, request_id VARCHAR(128) NOT NULL,
  business_date DATE NOT NULL, rule_version_id BIGINT NOT NULL, status VARCHAR(32) NOT NULL, source_count INT NOT NULL DEFAULT 0,
  settlement_amount_fen BIGINT NOT NULL DEFAULT 0, started_time DATETIME(3) NOT NULL, completed_time DATETIME(3), create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_settlement_batch_no(batch_no), UNIQUE KEY uk_settlement_request(tenant_id,request_id),
  KEY idx_settlement_batch_date(tenant_id,business_date)
);
CREATE TABLE IF NOT EXISTS finance_settlement_order (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, batch_id BIGINT NOT NULL, settlement_order_no VARCHAR(64) NOT NULL,
  source_id BIGINT NOT NULL, payment_no VARCHAR(64) NOT NULL, settlement_base_amount_fen BIGINT NOT NULL,
  allocated_amount_fen BIGINT NOT NULL, currency CHAR(3) NOT NULL DEFAULT 'CNY', status VARCHAR(32) NOT NULL, create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_settlement_order_no(settlement_order_no), UNIQUE KEY uk_settlement_source_order(source_id)
);
CREATE TABLE IF NOT EXISTS finance_settlement_detail (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, settlement_order_id BIGINT NOT NULL, participant_type VARCHAR(32) NOT NULL,
  participant_id VARCHAR(64) NOT NULL, amount_fen BIGINT NOT NULL, currency CHAR(3) NOT NULL DEFAULT 'CNY', create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), KEY idx_settlement_detail_order(settlement_order_id)
);
