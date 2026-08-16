CREATE TABLE fulfillment_order (
 id BIGINT PRIMARY KEY,
 merchant_order_id BIGINT NOT NULL,
 merchant_id BIGINT NOT NULL,
 warehouse_id BIGINT,
 status VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 KEY idx_fulfillment_order (merchant_order_id)
);
CREATE TABLE shipment_package (
 id BIGINT PRIMARY KEY,
 fulfillment_order_id BIGINT NOT NULL,
 package_no VARCHAR(64) NOT NULL,
 carrier_code VARCHAR(64),
 tracking_no VARCHAR(128),
 status VARCHAR(32) NOT NULL,
 shipped_at DATETIME(3),
 delivered_at DATETIME(3),
 UNIQUE KEY uk_package_no (package_no),
 KEY idx_tracking (carrier_code, tracking_no)
);
