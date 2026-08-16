CREATE TABLE provider_statement (
  id BIGINT PRIMARY KEY,
  provider_code VARCHAR(64) NOT NULL,
  statement_type VARCHAR(32) NOT NULL,
  statement_date DATE NOT NULL,
  currency CHAR(3) NOT NULL,
  source_file_ref VARCHAR(256),
  status VARCHAR(32) NOT NULL,
  imported_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_provider_statement (provider_code, statement_type, statement_date, currency)
);

CREATE TABLE provider_statement_item (
  id BIGINT PRIMARY KEY,
  statement_id BIGINT NOT NULL,
  provider_business_no VARCHAR(128) NOT NULL,
  provider_parent_no VARCHAR(128),
  business_type VARCHAR(32) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  fee_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  currency CHAR(3) NOT NULL,
  provider_status VARCHAR(32) NOT NULL,
  occurred_at DATETIME(3) NOT NULL,
  raw_line_json JSON,
  UNIQUE KEY uk_statement_business (statement_id, provider_business_no, business_type),
  KEY idx_statement_item_parent (provider_parent_no)
);
