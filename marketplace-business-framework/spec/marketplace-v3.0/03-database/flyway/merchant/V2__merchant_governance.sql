CREATE TABLE merchant_application (
  id BIGINT PRIMARY KEY,
  application_no VARCHAR(64) NOT NULL,
  applicant_user_id BIGINT NOT NULL,
  merchant_type VARCHAR(32) NOT NULL,
  proposed_legal_name VARCHAR(256) NULL,
  country_region_code VARCHAR(64) NULL,
  status VARCHAR(32) NOT NULL,
  current_verification_profile_id BIGINT NULL,
  current_risk_decision_id BIGINT NULL,
  workflow_instance_id VARCHAR(128) NULL,
  version INT NOT NULL DEFAULT 0,
  submitted_at DATETIME(3) NULL,
  approved_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_application_no (application_no),
  KEY idx_merchant_application_status (status, created_at)
);

CREATE TABLE merchant_verification_profile (
  id BIGINT PRIMARY KEY,
  application_id BIGINT NULL,
  merchant_id BIGINT NULL,
  profile_version INT NOT NULL,
  verification_type VARCHAR(32) NOT NULL,
  legal_name VARCHAR(256) NULL,
  registration_no_hash VARCHAR(128) NULL,
  registration_no_ciphertext VARCHAR(512) NULL,
  representative_name_ciphertext VARCHAR(512) NULL,
  identity_ref_hash VARCHAR(128) NULL,
  identity_ref_ciphertext VARCHAR(512) NULL,
  address_snapshot_json JSON NULL,
  tax_snapshot_json JSON NULL,
  settlement_profile_ref BIGINT NULL,
  evidence_file_ids_json JSON NULL,
  status VARCHAR(32) NOT NULL,
  verification_provider VARCHAR(64) NULL,
  verified_at DATETIME(3) NULL,
  expire_at DATETIME(3) NULL,
  snapshot_hash VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_verification_version (merchant_id, profile_version),
  KEY idx_merchant_verification_app (application_id, status)
);

CREATE TABLE merchant_settlement_profile (
  id BIGINT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  profile_version INT NOT NULL,
  provider_code VARCHAR(64) NOT NULL,
  account_name_ciphertext VARCHAR(512) NULL,
  account_no_ciphertext VARCHAR(512) NOT NULL,
  account_no_hash VARCHAR(128) NOT NULL,
  bank_or_wallet_code VARCHAR(128) NULL,
  verification_status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_settlement_profile (merchant_id, profile_version)
);

CREATE TABLE merchant_category_admission (
  id BIGINT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  shop_id BIGINT NULL,
  category_id BIGINT NOT NULL,
  region_code VARCHAR(64) NULL,
  qualification_snapshot_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  risk_restriction_json JSON NULL,
  workflow_instance_id VARCHAR(128) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_category_scope (merchant_id, shop_id, category_id, region_code),
  KEY idx_merchant_category_active (merchant_id, category_id, status, effective_from, effective_to)
);

CREATE TABLE merchant_deposit_transaction (
  id BIGINT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  deposit_account_id BIGINT NOT NULL,
  transaction_type VARCHAR(32) NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_id VARCHAR(128) NULL,
  currency CHAR(3) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  occurred_at DATETIME(3) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_deposit_tx_idem (deposit_account_id, idempotency_key),
  KEY idx_merchant_deposit_tx (merchant_id, occurred_at)
);

CREATE TABLE merchant_credit_profile (
  id BIGINT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  profile_date DATE NOT NULL,
  rule_version VARCHAR(64) NOT NULL,
  metric_snapshot_json JSON NOT NULL,
  credit_score DECIMAL(10,4) NOT NULL,
  merchant_level VARCHAR(32) NOT NULL,
  risk_level VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_credit_day (merchant_id, profile_date),
  KEY idx_merchant_credit_level (merchant_level, risk_level, profile_date)
);

CREATE TABLE merchant_exit_case (
  id BIGINT PRIMARY KEY,
  exit_no VARCHAR(64) NOT NULL,
  merchant_id BIGINT NOT NULL,
  request_type VARCHAR(32) NOT NULL,
  reason VARCHAR(1024) NULL,
  status VARCHAR(32) NOT NULL,
  blocker_snapshot_json JSON NULL,
  requested_by BIGINT NOT NULL,
  requested_at DATETIME(3) NOT NULL,
  closed_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_merchant_exit_no (exit_no),
  KEY idx_merchant_exit_merchant (merchant_id, status)
);
