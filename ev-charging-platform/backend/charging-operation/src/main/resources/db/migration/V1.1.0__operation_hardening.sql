CREATE TABLE IF NOT EXISTS operation_notification_policy (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  policy_code VARCHAR(64) NOT NULL,
  trigger_type VARCHAR(64) NOT NULL,
  min_severity VARCHAR(16) NOT NULL DEFAULT 'MAJOR',
  channel VARCHAR(32) NOT NULL,
  delay_minutes INT NOT NULL DEFAULT 0,
  recipient_type VARCHAR(32) NOT NULL,
  recipient_value VARCHAR(255) NOT NULL,
  enabled BIT NOT NULL DEFAULT 1,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_policy_code (tenant_id, policy_code),
  KEY idx_notification_policy_trigger (tenant_id, trigger_type, enabled)
);

CREATE TABLE IF NOT EXISTS operation_notification_task (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  task_no VARCHAR(64) NOT NULL,
  policy_id BIGINT NOT NULL DEFAULT 0,
  trigger_type VARCHAR(64) NOT NULL,
  business_no VARCHAR(64) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  recipient VARCHAR(255) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  scheduled_time DATETIME(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  last_error VARCHAR(1000),
  sent_time DATETIME(3),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_task_no (task_no),
  UNIQUE KEY uk_notification_business_policy (tenant_id, trigger_type, business_no, policy_id),
  KEY idx_notification_dispatch (status, scheduled_time)
);

CREATE TABLE IF NOT EXISTS operation_inspection_plan (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  plan_code VARCHAR(64) NOT NULL,
  plan_name VARCHAR(128) NOT NULL,
  station_id BIGINT NOT NULL,
  cycle_days INT NOT NULL,
  assignee_user_id BIGINT,
  checklist_json JSON NOT NULL,
  enabled BIT NOT NULL DEFAULT 1,
  next_generate_date DATE NOT NULL,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inspection_plan_code (tenant_id, plan_code),
  KEY idx_inspection_plan_due (tenant_id, enabled, next_generate_date)
);

CREATE TABLE IF NOT EXISTS operation_inspection_task (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  task_no VARCHAR(64) NOT NULL,
  plan_id BIGINT NOT NULL,
  station_id BIGINT NOT NULL,
  scheduled_date DATE NOT NULL,
  status VARCHAR(32) NOT NULL,
  assignee_user_id BIGINT,
  checklist_json JSON NOT NULL,
  result_json JSON,
  overdue BIT NOT NULL DEFAULT 0,
  started_time DATETIME(3),
  completed_time DATETIME(3),
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inspection_task_no (task_no),
  UNIQUE KEY uk_inspection_plan_date (plan_id, scheduled_date),
  KEY idx_inspection_task_assignee (tenant_id, assignee_user_id, status, scheduled_date),
  KEY idx_inspection_task_status (tenant_id, status, scheduled_date)
);

CREATE TABLE IF NOT EXISTS operation_spare_part (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  part_code VARCHAR(64) NOT NULL,
  part_name VARCHAR(128) NOT NULL,
  unit VARCHAR(32) NOT NULL,
  min_stock_qty INT NOT NULL DEFAULT 0,
  enabled BIT NOT NULL DEFAULT 1,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_spare_part_code (tenant_id, part_code)
);

CREATE TABLE IF NOT EXISTS operation_spare_stock (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  warehouse_code VARCHAR(64) NOT NULL,
  part_id BIGINT NOT NULL,
  available_qty INT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_spare_stock (tenant_id, warehouse_code, part_id),
  CHECK (available_qty >= 0)
);

CREATE TABLE IF NOT EXISTS operation_spare_stock_transaction (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  transaction_no VARCHAR(64) NOT NULL,
  request_id VARCHAR(128) NOT NULL,
  warehouse_code VARCHAR(64) NOT NULL,
  part_id BIGINT NOT NULL,
  change_type VARCHAR(32) NOT NULL,
  quantity_delta INT NOT NULL,
  balance_after INT NOT NULL,
  work_order_no VARCHAR(64),
  reference_no VARCHAR(64),
  operator_user_id BIGINT,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_spare_stock_tx_no (transaction_no),
  UNIQUE KEY uk_spare_stock_tx_request (tenant_id, request_id),
  KEY idx_spare_stock_tx (tenant_id, warehouse_code, part_id, create_time)
);

CREATE TABLE IF NOT EXISTS operation_work_order_attachment (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  work_order_id BIGINT NOT NULL,
  work_order_no VARCHAR(64) NOT NULL,
  object_key VARCHAR(255) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(128),
  size_bytes BIGINT NOT NULL,
  sha256 VARCHAR(64) NOT NULL,
  uploaded_by BIGINT,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_attachment_key (tenant_id, object_key),
  KEY idx_work_order_attachment (tenant_id, work_order_id, create_time)
);
