CREATE TABLE risk_rule_set (
  id BIGINT PRIMARY KEY,
  rule_set_code VARCHAR(64) NOT NULL,
  scenario VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  rule_definition_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_risk_rule_set_version (rule_set_code, version_no),
  KEY idx_risk_rule_set_active (scenario, status, effective_from, effective_to)
);

CREATE TABLE risk_feature_snapshot (
  id BIGINT PRIMARY KEY,
  scenario VARCHAR(64) NOT NULL,
  subject_type VARCHAR(32) NOT NULL,
  subject_id VARCHAR(128) NOT NULL,
  feature_version VARCHAR(64) NOT NULL,
  feature_json JSON NOT NULL,
  feature_hash VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_risk_feature_subject (scenario, subject_type, subject_id, created_at)
);

CREATE TABLE risk_decision (
  id BIGINT PRIMARY KEY,
  decision_no VARCHAR(64) NOT NULL,
  scenario VARCHAR(64) NOT NULL,
  subject_type VARCHAR(32) NOT NULL,
  subject_id VARCHAR(128) NOT NULL,
  merchant_id BIGINT NULL,
  user_id BIGINT NULL,
  decision VARCHAR(32) NOT NULL,
  risk_score DECIMAL(10,4) NULL,
  reason_codes_json JSON NOT NULL,
  rule_set_id BIGINT NOT NULL,
  rule_set_version INT NOT NULL,
  feature_snapshot_id BIGINT NOT NULL,
  challenge_type VARCHAR(32) NULL,
  hold_scope_json JSON NULL,
  expire_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_risk_decision_no (decision_no),
  KEY idx_risk_decision_subject (scenario, subject_type, subject_id, created_at)
);

CREATE TABLE risk_case (
  id BIGINT PRIMARY KEY,
  risk_case_no VARCHAR(64) NOT NULL,
  scenario VARCHAR(64) NOT NULL,
  merchant_id BIGINT NULL,
  user_id BIGINT NULL,
  subject_type VARCHAR(32) NOT NULL,
  subject_id VARCHAR(128) NOT NULL,
  source_decision_id BIGINT NOT NULL,
  severity VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  assigned_to BIGINT NULL,
  resolution VARCHAR(64) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_risk_case_no (risk_case_no),
  KEY idx_risk_case_status (status, severity, created_at)
);

CREATE TABLE abuse_signal_event (
  id BIGINT PRIMARY KEY,
  event_no VARCHAR(64) NOT NULL,
  signal_type VARCHAR(64) NOT NULL,
  subject_type VARCHAR(32) NOT NULL,
  subject_id VARCHAR(128) NOT NULL,
  merchant_id BIGINT NULL,
  user_id BIGINT NULL,
  device_fingerprint_ref VARCHAR(128) NULL,
  ip_hash VARCHAR(128) NULL,
  signal_json JSON NOT NULL,
  occurred_at DATETIME(3) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_abuse_signal_event_no (event_no),
  KEY idx_abuse_signal_subject (signal_type, subject_type, subject_id, occurred_at)
);
