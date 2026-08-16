CREATE TABLE checkout_session (
 id BIGINT PRIMARY KEY,
 checkout_token VARCHAR(128) NOT NULL,
 user_id BIGINT NOT NULL,
 session_json JSON NOT NULL,
 expire_at DATETIME(3) NOT NULL,
 status VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_checkout_token (checkout_token),
 KEY idx_checkout_user (user_id, expire_at)
);
