CREATE TABLE charging_session (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, operator_id BIGINT NOT NULL, session_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL, vehicle_id BIGINT NULL, station_id BIGINT NOT NULL, charger_id BIGINT NOT NULL, connector_id BIGINT NOT NULL,
  device_id VARCHAR(128) NOT NULL, connector_no INT NOT NULL,
  status TINYINT NOT NULL, charging_start_time DATETIME(3), charging_end_time DATETIME(3), initial_meter_wh BIGINT, final_meter_wh BIGINT,
  energy_wh BIGINT NOT NULL DEFAULT 0, initial_soc SMALLINT, final_soc SMALLINT, billing_snapshot_id BIGINT,
  version INT NOT NULL DEFAULT 0, create_time DATETIME(3) NOT NULL, update_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_core_session_no(session_no), KEY idx_core_connector_status(connector_id,status), KEY idx_core_status_update(status,update_time)
);
CREATE TABLE connector_active_session (
  connector_id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, session_id BIGINT NOT NULL, session_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL, create_time DATETIME(3) NOT NULL, PRIMARY KEY(connector_id), UNIQUE KEY uk_core_active_session(session_id)
);
CREATE TABLE charging_billing_snapshot (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, session_id BIGINT NOT NULL, billing_template_id BIGINT NOT NULL,
  billing_version_id BIGINT NOT NULL, snapshot_json JSON NOT NULL, snapshot_hash VARCHAR(64) NOT NULL, create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_core_billing_session(session_id)
);
CREATE TABLE charging_meter_record (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, session_id BIGINT NOT NULL, session_no VARCHAR(64) NOT NULL,
  record_type TINYINT NOT NULL, meter_wh BIGINT NOT NULL, soc SMALLINT, power_w BIGINT, record_time DATETIME(3) NOT NULL,
  source TINYINT NOT NULL, validation_status TINYINT NOT NULL, create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), KEY idx_core_meter_session_time(session_id,record_time)
);
CREATE TABLE charge_order (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, order_no VARCHAR(64) NOT NULL, session_id BIGINT NOT NULL, session_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL, station_id BIGINT NOT NULL, charger_id BIGINT NOT NULL, connector_id BIGINT NOT NULL, energy_wh BIGINT NOT NULL,
  energy_amount_fen BIGINT NOT NULL, service_amount_fen BIGINT NOT NULL, parking_amount_fen BIGINT NOT NULL DEFAULT 0,
  occupation_amount_fen BIGINT NOT NULL DEFAULT 0, original_amount_fen BIGINT NOT NULL, discount_amount_fen BIGINT NOT NULL DEFAULT 0,
  receivable_amount_fen BIGINT NOT NULL, paid_amount_fen BIGINT NOT NULL DEFAULT 0, refunded_amount_fen BIGINT NOT NULL DEFAULT 0,
  trade_status TINYINT NOT NULL, payment_status TINYINT NOT NULL, refund_status TINYINT NOT NULL, invoice_status TINYINT NOT NULL,
  finish_time DATETIME(3), version INT NOT NULL DEFAULT 0, create_time DATETIME(3) NOT NULL, update_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_core_order_no(order_no), UNIQUE KEY uk_core_order_session(session_id)
);
CREATE TABLE device_command_outbox (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, command_id VARCHAR(64) NOT NULL, device_id VARCHAR(128) NOT NULL,
  connector_no INT NOT NULL, command_type VARCHAR(32) NOT NULL, payload JSON NOT NULL, status TINYINT NOT NULL,
  retry_count INT NOT NULL DEFAULT 0, last_error VARCHAR(512), expire_time DATETIME(3) NOT NULL,
  create_time DATETIME(3) NOT NULL, update_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_core_command_id(command_id), KEY idx_core_command_publish(status,expire_time)
);
CREATE TABLE api_idempotency (
  id BIGINT NOT NULL, tenant_id BIGINT NOT NULL, operation_type VARCHAR(64) NOT NULL, request_id VARCHAR(128) NOT NULL,
  resource_no VARCHAR(64) NOT NULL, create_time DATETIME(3) NOT NULL, PRIMARY KEY(id), UNIQUE KEY uk_core_idempotency(tenant_id,operation_type,request_id)
);

CREATE TABLE charging_device_event_inbox (
  id BIGINT NOT NULL, event_id VARCHAR(64) NOT NULL, event_type VARCHAR(128) NOT NULL, processed_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_core_device_event_id(event_id)
);
