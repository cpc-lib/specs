CREATE TABLE IF NOT EXISTS operation_event_inbox (
  id BIGINT NOT NULL,
  consumer_name VARCHAR(64) NOT NULL,
  event_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(128) NOT NULL,
  processed_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_operation_consumer_event (consumer_name, event_id)
);

CREATE TABLE IF NOT EXISTS operation_alarm_rule (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  alarm_code VARCHAR(64) NOT NULL,
  enabled BIT NOT NULL DEFAULT 1,
  min_severity VARCHAR(16) NOT NULL DEFAULT 'MAJOR',
  auto_work_order BIT NOT NULL DEFAULT 1,
  sla_policy_id BIGINT,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_alarm_rule (tenant_id, alarm_code)
);

CREATE TABLE IF NOT EXISTS operation_sla_policy (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  policy_code VARCHAR(64) NOT NULL,
  policy_name VARCHAR(128) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  response_minutes INT NOT NULL,
  resolution_minutes INT NOT NULL,
  enabled BIT NOT NULL DEFAULT 1,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sla_policy_code (tenant_id, policy_code)
);

CREATE TABLE IF NOT EXISTS operation_alarm (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  alarm_no VARCHAR(64) NOT NULL,
  fingerprint VARCHAR(255) NOT NULL,
  device_id VARCHAR(64) NOT NULL,
  connector_no INT,
  alarm_code VARCHAR(64) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  status VARCHAR(32) NOT NULL,
  metric_value VARCHAR(64),
  metric_unit VARCHAR(32),
  alarm_message VARCHAR(512),
  occurrence_count INT NOT NULL DEFAULT 1,
  first_occurred_time DATETIME(3) NOT NULL,
  last_occurred_time DATETIME(3) NOT NULL,
  recovered_time DATETIME(3),
  acknowledged_by BIGINT,
  acknowledged_time DATETIME(3),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_alarm_no (alarm_no),
  KEY idx_alarm_active (tenant_id, status, severity, last_occurred_time),
  KEY idx_alarm_device (tenant_id, device_id, connector_no, last_occurred_time)
);

CREATE TABLE IF NOT EXISTS operation_active_alarm (
  tenant_id BIGINT NOT NULL,
  fingerprint VARCHAR(255) NOT NULL,
  alarm_id BIGINT NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (tenant_id, fingerprint),
  UNIQUE KEY uk_active_alarm_id (alarm_id)
);

CREATE TABLE IF NOT EXISTS operation_alarm_occurrence (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  alarm_id BIGINT NOT NULL,
  event_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(32) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  metric_value VARCHAR(64),
  occurred_time DATETIME(3) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_alarm_occurrence_event (tenant_id, event_id),
  KEY idx_alarm_occurrence (alarm_id, occurred_time)
);

CREATE TABLE IF NOT EXISTS operation_work_order (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  work_order_no VARCHAR(64) NOT NULL,
  alarm_id BIGINT,
  alarm_no VARCHAR(64),
  title VARCHAR(255) NOT NULL,
  priority VARCHAR(16) NOT NULL,
  status VARCHAR(32) NOT NULL,
  assignee_user_id BIGINT,
  dispatcher_user_id BIGINT,
  verifier_user_id BIGINT,
  process_instance_id VARCHAR(64),
  response_due_time DATETIME(3) NOT NULL,
  resolution_due_time DATETIME(3) NOT NULL,
  first_response_time DATETIME(3),
  repair_started_time DATETIME(3),
  repair_completed_time DATETIME(3),
  resolved_time DATETIME(3),
  repair_summary VARCHAR(1000),
  verify_comment VARCHAR(1000),
  sla_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_no (work_order_no),
  UNIQUE KEY uk_work_order_alarm (tenant_id, alarm_id),
  KEY idx_work_order_status (tenant_id, status, priority, create_time),
  KEY idx_work_order_assignee (tenant_id, assignee_user_id, status)
);

CREATE TABLE IF NOT EXISTS operation_work_order_event (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  work_order_id BIGINT NOT NULL,
  work_order_no VARCHAR(64) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  operator_user_id BIGINT,
  event_payload JSON,
  occurred_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_work_order_event (work_order_id, occurred_time)
);

CREATE TABLE IF NOT EXISTS operation_sla_breach (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  work_order_id BIGINT NOT NULL,
  work_order_no VARCHAR(64) NOT NULL,
  breach_type VARCHAR(32) NOT NULL,
  due_time DATETIME(3) NOT NULL,
  detected_time DATETIME(3) NOT NULL,
  acknowledged BIT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_breach (work_order_id, breach_type),
  KEY idx_sla_breach_open (tenant_id, acknowledged, detected_time)
);
