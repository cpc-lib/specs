CREATE TABLE promotion_quota_reservation (
 id BIGINT PRIMARY KEY,
 campaign_id BIGINT NOT NULL,
 buyer_id BIGINT NOT NULL,
 trade_no VARCHAR(64) NOT NULL,
 reserved_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
 reserved_quantity BIGINT NOT NULL DEFAULT 0,
 status VARCHAR(32) NOT NULL,
 expire_at DATETIME(3) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_promotion_reservation (campaign_id, trade_no),
 KEY idx_promo_reservation_expire (status, expire_at)
);
