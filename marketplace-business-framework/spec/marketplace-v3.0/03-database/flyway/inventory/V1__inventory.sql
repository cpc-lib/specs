CREATE TABLE inventory_stock (
 id BIGINT PRIMARY KEY,
 sku_id BIGINT NOT NULL,
 warehouse_id BIGINT NOT NULL,
 on_hand_qty BIGINT NOT NULL DEFAULT 0,
 available_qty BIGINT NOT NULL DEFAULT 0,
 reserved_qty BIGINT NOT NULL DEFAULT 0,
 locked_qty BIGINT NOT NULL DEFAULT 0,
 frozen_qty BIGINT NOT NULL DEFAULT 0,
 version BIGINT NOT NULL DEFAULT 0,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_stock_sku_wh (sku_id, warehouse_id)
);
CREATE TABLE inventory_reservation (
 id BIGINT PRIMARY KEY,
 reservation_no VARCHAR(64) NOT NULL,
 sku_id BIGINT NOT NULL,
 warehouse_id BIGINT NOT NULL,
 business_type VARCHAR(32) NOT NULL,
 business_id VARCHAR(128) NOT NULL,
 quantity BIGINT NOT NULL,
 expire_at DATETIME(3) NOT NULL,
 status VARCHAR(32) NOT NULL,
 idempotency_key VARCHAR(128) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_inventory_reservation_no (reservation_no),
 UNIQUE KEY uk_inventory_reservation_idem (idempotency_key),
 KEY idx_inventory_reservation_expire (status, expire_at)
);
CREATE TABLE inventory_ledger (
 id BIGINT PRIMARY KEY,
 sku_id BIGINT NOT NULL,
 warehouse_id BIGINT NOT NULL,
 biz_type VARCHAR(32) NOT NULL,
 biz_id VARCHAR(128) NOT NULL,
 change_on_hand BIGINT NOT NULL DEFAULT 0,
 change_available BIGINT NOT NULL DEFAULT 0,
 change_reserved BIGINT NOT NULL DEFAULT 0,
 balance_available BIGINT NOT NULL,
 occurred_at DATETIME(3) NOT NULL,
 KEY idx_inventory_ledger_sku (sku_id, warehouse_id, occurred_at),
 KEY idx_inventory_ledger_biz (biz_type, biz_id)
);
