CREATE TABLE shop (
 id BIGINT PRIMARY KEY,
 merchant_id BIGINT NOT NULL,
 shop_no VARCHAR(64) NOT NULL,
 shop_name VARCHAR(256) NOT NULL,
 shop_type VARCHAR(32) NOT NULL,
 status VARCHAR(32) NOT NULL,
 rating DECIMAL(8,4),
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_shop_no (shop_no),
 KEY idx_shop_merchant (merchant_id, status)
);
