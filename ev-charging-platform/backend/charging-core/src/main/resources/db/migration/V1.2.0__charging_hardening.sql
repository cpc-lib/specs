CREATE TABLE billing_template (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  template_name VARCHAR(128) NOT NULL,
  timezone VARCHAR(64) NOT NULL,
  status TINYINT NOT NULL,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_billing_template_tenant_status (tenant_id,status)
);

CREATE TABLE billing_version (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  template_id BIGINT NOT NULL,
  version_no VARCHAR(64) NOT NULL,
  status TINYINT NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_billing_version_no (tenant_id,version_no),
  KEY idx_billing_version_effective (tenant_id,status,effective_from,effective_to)
);

CREATE TABLE billing_period (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  version_id BIGINT NOT NULL,
  sequence_no INT NOT NULL,
  period_type VARCHAR(16) NOT NULL,
  start_minute INT NOT NULL,
  end_minute INT NOT NULL,
  energy_price_micro BIGINT NOT NULL,
  service_price_micro BIGINT NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_billing_period_sequence (version_id,sequence_no),
  KEY idx_billing_period_version (version_id,start_minute,end_minute)
);

CREATE TABLE billing_station_binding (
  station_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  billing_version_id BIGINT NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (station_id),
  KEY idx_billing_binding_tenant (tenant_id,billing_version_id)
);

CREATE TABLE charging_segment (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  session_id BIGINT NOT NULL,
  segment_no INT NOT NULL,
  period_type VARCHAR(16) NOT NULL,
  start_time DATETIME(3) NOT NULL,
  end_time DATETIME(3) NOT NULL,
  start_meter_wh BIGINT NOT NULL,
  end_meter_wh BIGINT NOT NULL,
  energy_wh BIGINT NOT NULL,
  energy_price_micro BIGINT NOT NULL,
  service_price_micro BIGINT NOT NULL,
  energy_amount_fen BIGINT NOT NULL,
  service_amount_fen BIGINT NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_charging_segment_no (session_id,segment_no),
  KEY idx_charging_segment_session (tenant_id,session_id)
);

CREATE TABLE charging_billing_result (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  session_id BIGINT NOT NULL,
  snapshot_id BIGINT NOT NULL,
  energy_wh BIGINT NOT NULL,
  energy_amount_fen BIGINT NOT NULL,
  service_amount_fen BIGINT NOT NULL,
  parking_amount_fen BIGINT NOT NULL DEFAULT 0,
  occupation_amount_fen BIGINT NOT NULL DEFAULT 0,
  discount_amount_fen BIGINT NOT NULL DEFAULT 0,
  receivable_amount_fen BIGINT NOT NULL,
  result_hash VARCHAR(64) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_charging_billing_result_session (session_id)
);

CREATE TABLE charging_recovery_record (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  session_id BIGINT NOT NULL,
  session_no VARCHAR(64) NOT NULL,
  original_status TINYINT NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  attempt_no INT NOT NULL,
  result_type VARCHAR(32) NOT NULL,
  detail VARCHAR(512),
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_recovery_session_time (session_id,create_time)
);

ALTER TABLE charging_session
  ADD COLUMN recovery_count INT NOT NULL DEFAULT 0 AFTER version,
  ADD COLUMN last_recovery_time DATETIME(3) NULL AFTER recovery_count;
