CREATE TABLE offer_price (
 id BIGINT PRIMARY KEY,
 offer_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 currency CHAR(3) NOT NULL,
 base_price DECIMAL(18,2) NOT NULL,
 sale_price DECIMAL(18,2) NOT NULL,
 effective_from DATETIME(3) NOT NULL,
 effective_to DATETIME(3),
 version_no INT NOT NULL,
 status VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_offer_price_version (offer_id, version_no),
 KEY idx_offer_price_active (offer_id, status, effective_from, effective_to)
);
