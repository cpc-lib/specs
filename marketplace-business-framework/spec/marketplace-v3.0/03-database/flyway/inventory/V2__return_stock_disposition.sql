CREATE TABLE inventory_return_inbound (
  id BIGINT PRIMARY KEY,
  inbound_no VARCHAR(64) NOT NULL,
  return_order_id BIGINT NOT NULL,
  inspection_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_inventory_return_inbound_no (inbound_no),
  UNIQUE KEY uk_inventory_return_inspection (return_order_id, inspection_id, sku_id)
);

CREATE TABLE return_stock_disposition (
  id BIGINT PRIMARY KEY,
  return_order_id BIGINT NOT NULL,
  inspection_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  disposition_type VARCHAR(32) NOT NULL,
  reason_code VARCHAR(64) NULL,
  operator_id BIGINT NULL,
  occurred_at DATETIME(3) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_return_disposition_return (return_order_id, occurred_at)
);
