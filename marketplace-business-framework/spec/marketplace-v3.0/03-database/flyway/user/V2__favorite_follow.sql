CREATE TABLE user_favorite_offer (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  offer_id BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_user_favorite_offer (user_id, offer_id),
  KEY idx_favorite_offer (offer_id, created_at)
);

CREATE TABLE user_shop_follow (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_user_shop_follow (user_id, shop_id),
  KEY idx_shop_follow (shop_id, created_at)
);
