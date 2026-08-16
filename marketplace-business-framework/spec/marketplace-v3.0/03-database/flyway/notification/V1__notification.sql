CREATE TABLE notification_preference (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category VARCHAR(32) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  enabled_flag TINYINT NOT NULL DEFAULT 1,
  quiet_hours_json JSON NULL,
  version INT NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_notification_preference (user_id, category, channel)
);

CREATE TABLE notification_template_version (
  id BIGINT PRIMARY KEY,
  template_code VARCHAR(64) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  locale VARCHAR(32) NOT NULL,
  version_no INT NOT NULL,
  subject_template VARCHAR(512) NULL,
  body_template_ref VARCHAR(512) NOT NULL,
  variable_schema_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_notification_template_ver (template_code, channel, locale, version_no)
);

CREATE TABLE notification_message (
  id BIGINT PRIMARY KEY,
  message_no VARCHAR(64) NOT NULL,
  business_type VARCHAR(64) NOT NULL,
  business_id VARCHAR(128) NOT NULL,
  recipient_type VARCHAR(32) NOT NULL,
  recipient_ref VARCHAR(128) NOT NULL,
  category VARCHAR(32) NOT NULL,
  template_code VARCHAR(64) NOT NULL,
  template_version INT NOT NULL,
  trigger_key VARCHAR(128) NOT NULL,
  payload_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_notification_message_no (message_no),
  UNIQUE KEY uk_notification_dedup (business_type, business_id, recipient_ref, template_code, trigger_key)
);

CREATE TABLE notification_delivery (
  id BIGINT PRIMARY KEY,
  message_id BIGINT NOT NULL,
  channel VARCHAR(32) NOT NULL,
  recipient_address_hash VARCHAR(128) NULL,
  recipient_snapshot_masked VARCHAR(256) NULL,
  provider_code VARCHAR(64) NULL,
  provider_message_id VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at DATETIME(3) NULL,
  sent_at DATETIME(3) NULL,
  delivered_at DATETIME(3) NULL,
  last_error VARCHAR(1024) NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_notification_delivery (message_id, channel, recipient_address_hash),
  KEY idx_notification_retry (status, next_retry_at)
);
