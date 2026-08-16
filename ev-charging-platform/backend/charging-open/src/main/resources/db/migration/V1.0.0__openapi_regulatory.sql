CREATE TABLE IF NOT EXISTS open_partner_app (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  partner_code VARCHAR(64) NOT NULL,
  partner_name VARCHAR(128) NOT NULL,
  app_key VARCHAR(96) NOT NULL,
  secret_ciphertext VARCHAR(1024) NOT NULL,
  status VARCHAR(32) NOT NULL,
  data_scope_type VARCHAR(32) NOT NULL,
  rate_limit_per_minute INT NOT NULL DEFAULT 120,
  callback_url VARCHAR(512),
  callback_secret_ciphertext VARCHAR(1024),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_open_partner_code (tenant_id,partner_code),
  UNIQUE KEY uk_open_app_key (app_key)
);

CREATE TABLE IF NOT EXISTS open_partner_scope (
  partner_id BIGINT NOT NULL,
  scope_code VARCHAR(128) NOT NULL,
  PRIMARY KEY (partner_id,scope_code)
);

CREATE TABLE IF NOT EXISTS open_partner_station_scope (
  partner_id BIGINT NOT NULL,
  station_id BIGINT NOT NULL,
  PRIMARY KEY (partner_id,station_id)
);

CREATE TABLE IF NOT EXISTS open_partner_user_mapping (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  partner_id BIGINT NOT NULL,
  external_user_id VARCHAR(128) NOT NULL,
  local_user_id BIGINT NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_partner_external_user (partner_id,external_user_id),
  UNIQUE KEY uk_partner_local_user (tenant_id,local_user_id)
);

CREATE TABLE IF NOT EXISTS open_partner_charging_ref (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  partner_id BIGINT NOT NULL,
  partner_request_id VARCHAR(128) NOT NULL,
  external_user_id VARCHAR(128) NOT NULL,
  session_no VARCHAR(64) NOT NULL,
  connector_code VARCHAR(64) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_partner_request (partner_id,partner_request_id),
  UNIQUE KEY uk_partner_session (partner_id,session_no)
);

CREATE TABLE IF NOT EXISTS open_api_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT,
  partner_id BIGINT,
  app_key VARCHAR(96),
  request_id VARCHAR(96),
  method VARCHAR(16) NOT NULL,
  request_path VARCHAR(512) NOT NULL,
  request_body_sha256 CHAR(64),
  response_status INT NOT NULL,
  latency_ms BIGINT NOT NULL,
  remote_ip VARCHAR(128),
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_open_audit_partner_time (partner_id,create_time),
  KEY idx_open_audit_request (request_id)
);

CREATE TABLE IF NOT EXISTS open_partner_callback_task (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  partner_id BIGINT NOT NULL,
  callback_type VARCHAR(64) NOT NULL,
  business_key VARCHAR(128) NOT NULL,
  payload_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_time DATETIME(3) NOT NULL,
  response_status INT,
  response_body VARCHAR(2000),
  last_error VARCHAR(1000),
  sent_time DATETIME(3),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_partner_callback_business (partner_id,callback_type,business_key),
  KEY idx_partner_callback_dispatch (status,next_retry_time)
);

CREATE TABLE IF NOT EXISTS open_regulatory_platform (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  platform_code VARCHAR(64) NOT NULL,
  platform_name VARCHAR(128) NOT NULL,
  protocol_code VARCHAR(64) NOT NULL,
  endpoint_url VARCHAR(512) NOT NULL,
  credential_key VARCHAR(128),
  credential_secret_ciphertext VARCHAR(1024),
  enabled BIT NOT NULL DEFAULT 1,
  public_info_enabled BIT NOT NULL DEFAULT 1,
  business_info_enabled BIT NOT NULL DEFAULT 1,
  rate_limit_per_minute INT NOT NULL DEFAULT 120,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_regulatory_platform (tenant_id,platform_code)
);

CREATE TABLE IF NOT EXISTS open_regulatory_report_task (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  platform_id BIGINT NOT NULL,
  data_type VARCHAR(64) NOT NULL,
  business_key VARCHAR(128) NOT NULL,
  source_payload_json JSON NOT NULL,
  payload_hash CHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_time DATETIME(3) NOT NULL,
  response_status INT,
  response_body VARCHAR(2000),
  last_error VARCHAR(1000),
  reported_time DATETIME(3),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_regulatory_report (platform_id,data_type,business_key,payload_hash),
  KEY idx_regulatory_dispatch (status,next_retry_time)
);
