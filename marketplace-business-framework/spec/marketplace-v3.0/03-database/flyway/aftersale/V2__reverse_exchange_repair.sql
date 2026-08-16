CREATE TABLE return_order (
  id BIGINT PRIMARY KEY,
  return_no VARCHAR(64) NOT NULL,
  aftersale_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  merchant_order_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  expected_qty INT NOT NULL,
  received_qty INT NOT NULL DEFAULT 0,
  return_address_snapshot_json JSON NOT NULL,
  required_ship_before DATETIME(3) NULL,
  status VARCHAR(32) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_return_no (return_no),
  KEY idx_return_aftersale (aftersale_id),
  KEY idx_return_deadline (status, required_ship_before)
);

CREATE TABLE reverse_shipment (
  id BIGINT PRIMARY KEY,
  reverse_shipment_no VARCHAR(64) NOT NULL,
  return_order_id BIGINT NOT NULL,
  pickup_mode VARCHAR(32) NOT NULL,
  carrier_code VARCHAR(64) NULL,
  tracking_no VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL,
  picked_up_at DATETIME(3) NULL,
  delivered_at DATETIME(3) NULL,
  received_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_reverse_shipment_no (reverse_shipment_no),
  KEY idx_reverse_return (return_order_id, status)
);

CREATE TABLE return_inspection (
  id BIGINT PRIMARY KEY,
  inspection_no VARCHAR(64) NOT NULL,
  return_order_id BIGINT NOT NULL,
  inspected_qty INT NOT NULL,
  accepted_qty INT NOT NULL,
  rejected_qty INT NOT NULL,
  result_code VARCHAR(64) NOT NULL,
  result_json JSON NOT NULL,
  inspector_id BIGINT NULL,
  inspected_at DATETIME(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_return_inspection_no (inspection_no),
  UNIQUE KEY uk_return_inspection_active (return_order_id, status)
);

CREATE TABLE exchange_order (
  id BIGINT PRIMARY KEY,
  exchange_no VARCHAR(64) NOT NULL,
  aftersale_id BIGINT NOT NULL,
  original_order_item_id BIGINT NOT NULL,
  replacement_sku_id BIGINT NOT NULL,
  replacement_qty INT NOT NULL,
  inventory_reservation_no VARCHAR(64) NULL,
  fulfillment_no VARCHAR(64) NULL,
  price_difference_policy VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_exchange_no (exchange_no),
  KEY idx_exchange_aftersale (aftersale_id, status)
);

CREATE TABLE repair_order (
  id BIGINT PRIMARY KEY,
  repair_no VARCHAR(64) NOT NULL,
  aftersale_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  service_provider_id BIGINT NULL,
  fault_snapshot_json JSON NOT NULL,
  inspection_snapshot_json JSON NULL,
  repair_result_json JSON NULL,
  promised_complete_at DATETIME(3) NULL,
  status VARCHAR(32) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_repair_no (repair_no),
  KEY idx_repair_aftersale (aftersale_id, status),
  KEY idx_repair_sla (status, promised_complete_at)
);

CREATE TABLE aftersale_timeout_clock (
  id BIGINT PRIMARY KEY,
  aftersale_id BIGINT NOT NULL,
  clock_type VARCHAR(64) NOT NULL,
  deadline_at DATETIME(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  policy_version VARCHAR(64) NOT NULL,
  fired_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_aftersale_clock (aftersale_id, clock_type),
  KEY idx_aftersale_clock_due (status, deadline_at)
);

CREATE TABLE aftersale_evidence (
  id BIGINT PRIMARY KEY,
  aftersale_id BIGINT NULL,
  dispute_id BIGINT NULL,
  submitted_by_type VARCHAR(32) NOT NULL,
  submitted_by_id BIGINT NOT NULL,
  evidence_type VARCHAR(64) NOT NULL,
  file_id BIGINT NULL,
  reference_type VARCHAR(64) NULL,
  reference_id VARCHAR(128) NULL,
  sha256 VARCHAR(128) NULL,
  description VARCHAR(1024) NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_evidence_aftersale (aftersale_id, created_at),
  KEY idx_evidence_dispute (dispute_id, created_at)
);
