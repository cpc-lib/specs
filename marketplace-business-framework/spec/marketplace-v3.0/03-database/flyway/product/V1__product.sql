CREATE TABLE platform_category (
 id BIGINT PRIMARY KEY,
 parent_id BIGINT,
 category_code VARCHAR(64) NOT NULL,
 category_name VARCHAR(128) NOT NULL,
 level INT NOT NULL,
 status VARCHAR(32) NOT NULL,
 UNIQUE KEY uk_category_code (category_code),
 KEY idx_category_parent (parent_id, status)
);
CREATE TABLE brand (
 id BIGINT PRIMARY KEY,
 brand_code VARCHAR(64) NOT NULL,
 brand_name VARCHAR(128) NOT NULL,
 status VARCHAR(32) NOT NULL,
 UNIQUE KEY uk_brand_code (brand_code)
);
CREATE TABLE product_spu (
 id BIGINT PRIMARY KEY,
 category_id BIGINT NOT NULL,
 brand_id BIGINT,
 spu_no VARCHAR(64) NOT NULL,
 standard_name VARCHAR(256) NOT NULL,
 status VARCHAR(32) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_spu_no (spu_no),
 KEY idx_spu_category (category_id, status)
);
CREATE TABLE product_sku (
 id BIGINT PRIMARY KEY,
 spu_id BIGINT NOT NULL,
 sku_no VARCHAR(64) NOT NULL,
 spec_json JSON NOT NULL,
 barcode VARCHAR(128),
 weight_grams INT,
 status VARCHAR(32) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_sku_no (sku_no),
 KEY idx_sku_spu (spu_id, status)
);
CREATE TABLE merchant_offer (
 id BIGINT PRIMARY KEY,
 merchant_id BIGINT NOT NULL,
 shop_id BIGINT NOT NULL,
 spu_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 offer_no VARCHAR(64) NOT NULL,
 title VARCHAR(512) NOT NULL,
 main_image_file_id BIGINT,
 status VARCHAR(32) NOT NULL,
 data_version BIGINT NOT NULL DEFAULT 0,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_offer_no (offer_no),
 KEY idx_offer_shop (shop_id, status),
 KEY idx_offer_sku (sku_id, status)
);
