CREATE TABLE user_account (
 id BIGINT PRIMARY KEY,
 status VARCHAR(32) NOT NULL,
 nickname VARCHAR(128),
 avatar_file_id BIGINT,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL
);
CREATE TABLE user_address (
 id BIGINT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 consignee VARCHAR(128) NOT NULL,
 phone_ciphertext VARCHAR(512) NOT NULL,
 phone_hash VARCHAR(128) NOT NULL,
 province VARCHAR(64) NOT NULL,
 city VARCHAR(64) NOT NULL,
 district VARCHAR(64) NOT NULL,
 street VARCHAR(128),
 detail_address VARCHAR(512) NOT NULL,
 longitude DECIMAL(10,7),
 latitude DECIMAL(10,7),
 is_default TINYINT NOT NULL DEFAULT 0,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 KEY idx_user_address_user (user_id, is_default)
);
