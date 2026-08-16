CREATE TABLE review (
 id BIGINT PRIMARY KEY,
 buyer_id BIGINT NOT NULL,
 merchant_id BIGINT NOT NULL,
 offer_id BIGINT NOT NULL,
 order_item_id BIGINT NOT NULL,
 product_score INT,
 service_score INT,
 logistics_score INT,
 content VARCHAR(4000),
 status VARCHAR(32) NOT NULL,
 verified_purchase TINYINT NOT NULL DEFAULT 1,
 created_at DATETIME(3) NOT NULL,
 updated_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_review_order_item (buyer_id, order_item_id),
 KEY idx_review_offer (offer_id, status, created_at)
);
