ALTER TABLE fulfillment_order
  ADD COLUMN shop_id BIGINT NULL AFTER merchant_id,
  ADD COLUMN fulfillment_no VARCHAR(64) NULL AFTER id,
  ADD COLUMN fulfillment_type VARCHAR(32) NOT NULL DEFAULT 'PHYSICAL' AFTER shop_id,
  ADD COLUMN fulfillment_mode VARCHAR(32) NOT NULL DEFAULT 'MERCHANT' AFTER fulfillment_type,
  ADD COLUMN route_snapshot_id BIGINT NULL AFTER warehouse_id,
  ADD COLUMN promised_ship_at DATETIME(3) NULL,
  ADD COLUMN promised_deliver_at DATETIME(3) NULL,
  ADD COLUMN version INT NOT NULL DEFAULT 0,
  ADD UNIQUE KEY uk_fulfillment_no (fulfillment_no);

CREATE TABLE fulfillment_route_snapshot (
  id BIGINT PRIMARY KEY,
  merchant_order_id BIGINT NOT NULL,
  route_version INT NOT NULL,
  destination_region_code VARCHAR(64) NULL,
  selected_route_json JSON NOT NULL,
  score_trace_json JSON NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_fulfillment_route_version (merchant_order_id, route_version)
);

CREATE TABLE fulfillment_order_item (
  id BIGINT PRIMARY KEY,
  fulfillment_order_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  ordered_qty INT NOT NULL,
  fulfill_qty INT NOT NULL,
  cancelled_qty INT NOT NULL DEFAULT 0,
  shipped_qty INT NOT NULL DEFAULT 0,
  received_qty INT NOT NULL DEFAULT 0,
  returned_qty INT NOT NULL DEFAULT 0,
  exchanged_qty INT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_fulfillment_item (fulfillment_order_id, order_item_id),
  KEY idx_fulfillment_item_order_item (order_item_id)
);

CREATE TABLE fulfillment_package (
  id BIGINT PRIMARY KEY,
  fulfillment_order_id BIGINT NOT NULL,
  package_no VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  weight_grams INT NULL,
  volume_cm3 BIGINT NULL,
  packed_at DATETIME(3) NULL,
  handed_over_at DATETIME(3) NULL,
  delivered_at DATETIME(3) NULL,
  received_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_fulfillment_package_no (package_no),
  KEY idx_fulfillment_package_order (fulfillment_order_id, status)
);

CREATE TABLE fulfillment_package_item (
  id BIGINT PRIMARY KEY,
  package_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  UNIQUE KEY uk_fulfillment_package_item (package_id, order_item_id),
  KEY idx_fulfillment_package_item_order (order_item_id)
);

CREATE TABLE shipment (
  id BIGINT PRIMARY KEY,
  package_id BIGINT NOT NULL,
  shipment_no VARCHAR(64) NOT NULL,
  shipment_leg_no INT NOT NULL DEFAULT 1,
  carrier_code VARCHAR(64) NOT NULL,
  service_code VARCHAR(64) NULL,
  tracking_no VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL,
  ship_from_snapshot_json JSON NOT NULL,
  ship_to_snapshot_json JSON NOT NULL,
  label_file_id BIGINT NULL,
  shipped_at DATETIME(3) NULL,
  delivered_at DATETIME(3) NULL,
  exception_code VARCHAR(64) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_shipment_no (shipment_no),
  UNIQUE KEY uk_shipment_tracking_leg (carrier_code, tracking_no, shipment_leg_no),
  KEY idx_shipment_package (package_id, status)
);

CREATE TABLE shipment_tracking_event (
  id BIGINT PRIMARY KEY,
  shipment_id BIGINT NOT NULL,
  carrier_code VARCHAR(64) NOT NULL,
  tracking_no VARCHAR(128) NOT NULL,
  provider_event_id VARCHAR(128) NOT NULL,
  raw_status VARCHAR(128) NULL,
  normalized_status VARCHAR(32) NOT NULL,
  event_time DATETIME(3) NOT NULL,
  location_text VARCHAR(512) NULL,
  raw_payload_ref VARCHAR(512) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_tracking_provider_event (carrier_code, provider_event_id),
  KEY idx_tracking_shipment_time (shipment_id, event_time)
);

CREATE TABLE delivery_confirmation (
  id BIGINT PRIMARY KEY,
  merchant_order_id BIGINT NOT NULL,
  package_id BIGINT NULL,
  confirmation_type VARCHAR(32) NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_id VARCHAR(128) NULL,
  confirmed_at DATETIME(3) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_delivery_confirmation_idem (idempotency_key),
  KEY idx_delivery_confirmation_order (merchant_order_id, confirmed_at)
);

CREATE TABLE digital_fulfillment (
  id BIGINT PRIMARY KEY,
  digital_fulfillment_no VARCHAR(64) NOT NULL,
  merchant_order_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  fulfillment_type VARCHAR(32) NOT NULL,
  provider_code VARCHAR(64) NULL,
  provider_request_no VARCHAR(128) NULL,
  external_delivery_no VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL,
  entitlement_ref_ciphertext VARCHAR(1024) NULL,
  delivered_at DATETIME(3) NULL,
  consumed_at DATETIME(3) NULL,
  expire_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_digital_fulfillment_no (digital_fulfillment_no),
  UNIQUE KEY uk_digital_provider_req (provider_code, provider_request_no)
);
