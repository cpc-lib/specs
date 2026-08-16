ALTER TABLE review
  ADD COLUMN review_no VARCHAR(64) NULL AFTER id,
  ADD COLUMN shop_id BIGINT NULL AFTER merchant_id,
  ADD COLUMN risk_decision_ref VARCHAR(128) NULL,
  ADD COLUMN moderation_ref VARCHAR(128) NULL,
  ADD COLUMN published_at DATETIME(3) NULL,
  ADD UNIQUE KEY uk_review_no (review_no);

CREATE TABLE additional_review (
  id BIGINT PRIMARY KEY,
  review_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  content VARCHAR(4000) NULL,
  media_file_ids_json JSON NULL,
  moderation_status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_additional_review (review_id, created_at)
);

CREATE TABLE seller_review_reply (
  id BIGINT PRIMARY KEY,
  review_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  reply_version INT NOT NULL,
  content VARCHAR(4000) NOT NULL,
  moderation_status VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_review_reply_version (review_id, reply_version)
);

CREATE TABLE review_moderation_record (
  id BIGINT PRIMARY KEY,
  review_id BIGINT NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  decision VARCHAR(32) NOT NULL,
  reason_codes_json JSON NULL,
  rule_or_model_version VARCHAR(128) NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_review_moderation (review_id, created_at)
);

CREATE TABLE review_summary_projection (
  id BIGINT PRIMARY KEY,
  subject_type VARCHAR(32) NOT NULL,
  subject_id BIGINT NOT NULL,
  review_count BIGINT NOT NULL DEFAULT 0,
  verified_purchase_count BIGINT NOT NULL DEFAULT 0,
  rating_average DECIMAL(8,4) NULL,
  rating_distribution_json JSON NULL,
  tag_count_json JSON NULL,
  source_version BIGINT NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_review_summary_subject (subject_type, subject_id)
);
