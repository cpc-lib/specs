CREATE TABLE cart_item (
 id BIGINT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 offer_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 quantity INT NOT NULL,
 selected TINYINT NOT NULL DEFAULT 1,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_cart_user_offer (user_id, offer_id),
 KEY idx_cart_user (user_id, updated_at)
);
