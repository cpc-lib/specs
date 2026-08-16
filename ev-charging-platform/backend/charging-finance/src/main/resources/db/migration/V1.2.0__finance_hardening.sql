ALTER TABLE finance_ledger_entry
  ADD COLUMN subject_type VARCHAR(32) NULL AFTER account_code,
  ADD COLUMN subject_id VARCHAR(64) NULL AFTER subject_type,
  ADD KEY idx_ledger_subject_time (tenant_id, subject_type, subject_id, create_time);

ALTER TABLE finance_reconciliation_detail
  ADD COLUMN local_original_amount_fen BIGINT NULL AFTER local_amount_fen,
  ADD COLUMN local_adjustment_fen BIGINT NOT NULL DEFAULT 0 AFTER local_original_amount_fen,
  ADD COLUMN local_original_refund_fen BIGINT NULL AFTER local_refund_fen,
  ADD COLUMN local_refund_adjustment_fen BIGINT NOT NULL DEFAULT 0 AFTER local_original_refund_fen;

ALTER TABLE finance_settlement_order
  DROP INDEX uk_settlement_source_order,
  ADD KEY idx_settlement_source_order (source_id, status);

ALTER TABLE finance_settlement_batch
  ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER status,
  ADD COLUMN approved_by BIGINT NULL AFTER created_by,
  ADD COLUMN approved_time DATETIME(3) NULL AFTER approved_by,
  ADD COLUMN rejected_by BIGINT NULL AFTER approved_time,
  ADD COLUMN rejected_time DATETIME(3) NULL AFTER rejected_by,
  ADD COLUMN approval_comment VARCHAR(512) NULL AFTER rejected_time;

CREATE TABLE IF NOT EXISTS finance_reconciliation_schedule (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  channel VARCHAR(32) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  zone_id VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai',
  enabled BIT NOT NULL DEFAULT b'1',
  last_success_business_date DATE NULL,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_recon_schedule (tenant_id, channel, merchant_id)
);

CREATE TABLE IF NOT EXISTS finance_channel_bill_archive (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  batch_id BIGINT NOT NULL,
  object_key VARCHAR(512) NOT NULL,
  sha256 VARCHAR(64) NOT NULL,
  size_bytes BIGINT NOT NULL,
  media_type VARCHAR(128),
  original_file_name VARCHAR(255),
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_bill_archive_batch (batch_id),
  UNIQUE KEY uk_bill_archive_sha (tenant_id, sha256)
);

CREATE TABLE IF NOT EXISTS finance_adjustment_order (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  adjustment_no VARCHAR(64) NOT NULL,
  request_id VARCHAR(128) NOT NULL,
  adjustment_type VARCHAR(32) NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  amount_fen BIGINT NOT NULL,
  reason VARCHAR(512) NOT NULL,
  status VARCHAR(32) NOT NULL,
  reverses_adjustment_id BIGINT NULL,
  created_by BIGINT NOT NULL,
  approved_by BIGINT NULL,
  approved_time DATETIME(3) NULL,
  rejected_by BIGINT NULL,
  rejected_time DATETIME(3) NULL,
  posted_time DATETIME(3) NULL,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_adjustment_no (adjustment_no),
  UNIQUE KEY uk_adjustment_request (tenant_id, request_id),
  KEY idx_adjustment_payment (tenant_id, payment_no, status),
  KEY idx_adjustment_status (tenant_id, status, create_time)
);

CREATE TABLE IF NOT EXISTS finance_invoice_request (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  request_no VARCHAR(64) NOT NULL,
  request_id VARCHAR(128) NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  biz_order_no VARCHAR(64) NOT NULL,
  amount_fen BIGINT NOT NULL,
  title_type VARCHAR(32) NOT NULL,
  title_name VARCHAR(255) NOT NULL,
  tax_no VARCHAR(64),
  email VARCHAR(255),
  provider_code VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  failure_message VARCHAR(512),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invoice_request_no (request_no),
  UNIQUE KEY uk_invoice_request_id (tenant_id, request_id),
  UNIQUE KEY uk_invoice_request (request_id),
  KEY idx_invoice_payment (tenant_id, payment_no, status)
);

CREATE TABLE IF NOT EXISTS finance_invoice (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  request_id BIGINT NOT NULL,
  invoice_no VARCHAR(128) NOT NULL,
  provider_invoice_no VARCHAR(128) NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  biz_order_no VARCHAR(64) NOT NULL,
  amount_fen BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  pdf_url VARCHAR(1024),
  issued_time DATETIME(3) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invoice_no (invoice_no),
  UNIQUE KEY uk_provider_invoice_no (provider_invoice_no),
  KEY idx_invoice_payment (tenant_id, payment_no, status)
);


CREATE TABLE IF NOT EXISTS finance_invoice_active (
  tenant_id BIGINT NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  invoice_request_id BIGINT NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (tenant_id, payment_no),
  UNIQUE KEY uk_invoice_active_request (invoice_request_id)
);

CREATE TABLE IF NOT EXISTS finance_invoice_red_flush (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  red_no VARCHAR(64) NOT NULL,
  request_id VARCHAR(128) NOT NULL,
  invoice_id BIGINT NOT NULL,
  reason VARCHAR(512) NOT NULL,
  provider_red_no VARCHAR(128),
  status VARCHAR(32) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  completed_time DATETIME(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_invoice_red_no (red_no),
  UNIQUE KEY uk_invoice_red_request (tenant_id, request_id),
  UNIQUE KEY uk_invoice_red_invoice (invoice_id)
);
