CREATE TABLE promotion_campaign (
 id BIGINT PRIMARY KEY,
 owner_type VARCHAR(32) NOT NULL,
 merchant_id BIGINT,
 shop_id BIGINT,
 campaign_type VARCHAR(32) NOT NULL,
 name VARCHAR(256) NOT NULL,
 start_at DATETIME(3) NOT NULL,
 end_at DATETIME(3) NOT NULL,
 status VARCHAR(32) NOT NULL,
 budget_amount DECIMAL(18,2),
 reserved_budget DECIMAL(18,2) NOT NULL DEFAULT 0,
 used_budget DECIMAL(18,2) NOT NULL DEFAULT 0,
 version INT NOT NULL DEFAULT 0,
 KEY idx_campaign_time (status, start_at, end_at)
);
CREATE TABLE coupon_wallet (
 id BIGINT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 coupon_template_id BIGINT NOT NULL,
 coupon_code VARCHAR(64) NOT NULL,
 status VARCHAR(32) NOT NULL,
 lock_trade_no VARCHAR(64),
 expire_at DATETIME(3) NOT NULL,
 version INT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_coupon_code (coupon_code),
 KEY idx_coupon_user (user_id, status, expire_at)
);
