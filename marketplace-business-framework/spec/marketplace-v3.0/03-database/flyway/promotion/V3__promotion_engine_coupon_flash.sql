CREATE TABLE promotion_rule (
  id BIGINT PRIMARY KEY,
  campaign_id BIGINT NOT NULL,
  rule_code VARCHAR(64) NOT NULL,
  rule_type VARCHAR(32) NOT NULL,
  condition_json JSON NOT NULL,
  benefit_json JSON NOT NULL,
  funding_party VARCHAR(32) NOT NULL,
  priority INT NOT NULL DEFAULT 0,
  version_no INT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_promotion_rule_version (campaign_id, rule_code, version_no),
  KEY idx_promotion_rule_active (campaign_id, status, priority)
);

CREATE TABLE promotion_scope (
  id BIGINT PRIMARY KEY,
  campaign_id BIGINT NOT NULL,
  rule_id BIGINT NULL,
  scope_type VARCHAR(32) NOT NULL,
  scope_ref VARCHAR(128) NOT NULL,
  include_flag TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL,
  KEY idx_promotion_scope (campaign_id, rule_id, scope_type, scope_ref)
);

CREATE TABLE promotion_compatibility_rule (
  id BIGINT PRIMARY KEY,
  policy_code VARCHAR(64) NOT NULL,
  left_type VARCHAR(32) NOT NULL,
  right_type VARCHAR(32) NOT NULL,
  relation_type VARCHAR(32) NOT NULL,
  priority INT NOT NULL DEFAULT 0,
  version_no INT NOT NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  UNIQUE KEY uk_promo_compat_version (policy_code, left_type, right_type, version_no)
);

CREATE TABLE promotion_budget_account (
  id BIGINT PRIMARY KEY,
  campaign_id BIGINT NOT NULL,
  currency CHAR(3) NOT NULL,
  total_budget DECIMAL(18,2) NOT NULL,
  reserved_budget DECIMAL(18,2) NOT NULL DEFAULT 0,
  consumed_budget DECIMAL(18,2) NOT NULL DEFAULT 0,
  released_budget DECIMAL(18,2) NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_promotion_budget_campaign (campaign_id)
);

CREATE TABLE promotion_budget_reservation (
  id BIGINT PRIMARY KEY,
  reservation_no VARCHAR(64) NOT NULL,
  campaign_id BIGINT NOT NULL,
  business_type VARCHAR(32) NOT NULL,
  business_id VARCHAR(128) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  expire_at DATETIME(3) NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_promo_budget_res_no (reservation_no),
  UNIQUE KEY uk_promo_budget_res_idem (idempotency_key),
  KEY idx_promo_budget_res_expire (status, expire_at)
);

CREATE TABLE promotion_quota (
  id BIGINT PRIMARY KEY,
  campaign_id BIGINT NOT NULL,
  rule_id BIGINT NULL,
  quota_type VARCHAR(32) NOT NULL,
  quota_key VARCHAR(128) NOT NULL,
  total_qty BIGINT NOT NULL,
  reserved_qty BIGINT NOT NULL DEFAULT 0,
  consumed_qty BIGINT NOT NULL DEFAULT 0,
  released_qty BIGINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_promotion_quota (campaign_id, quota_type, quota_key)
);

-- V2 already introduced promotion_quota_reservation for Trade-level promotion locks.
-- V2.4 evolves that table in-place so historical rows remain valid.
ALTER TABLE promotion_quota_reservation
  ADD COLUMN reservation_no VARCHAR(64) NULL AFTER id,
  ADD COLUMN quota_id BIGINT NULL AFTER campaign_id,
  ADD COLUMN business_type VARCHAR(32) NULL AFTER buyer_id,
  ADD COLUMN business_id VARCHAR(128) NULL AFTER business_type,
  ADD COLUMN idempotency_key VARCHAR(128) NULL AFTER expire_at;

UPDATE promotion_quota_reservation
SET reservation_no = CONCAT('LEGACY-PQR-', id)
WHERE reservation_no IS NULL;

UPDATE promotion_quota_reservation
SET business_type = 'TRADE',
    business_id = trade_no
WHERE business_type IS NULL OR business_id IS NULL;

UPDATE promotion_quota_reservation
SET idempotency_key = CONCAT('LEGACY-PQR-', id)
WHERE idempotency_key IS NULL;

ALTER TABLE promotion_quota_reservation
  MODIFY reservation_no VARCHAR(64) NOT NULL,
  MODIFY buyer_id BIGINT NULL,
  MODIFY trade_no VARCHAR(64) NULL,
  MODIFY business_type VARCHAR(32) NOT NULL,
  MODIFY business_id VARCHAR(128) NOT NULL,
  MODIFY expire_at DATETIME(3) NULL,
  MODIFY idempotency_key VARCHAR(128) NOT NULL,
  ADD UNIQUE KEY uk_promotion_quota_res_no (reservation_no),
  ADD UNIQUE KEY uk_promotion_quota_res_idem (idempotency_key),
  ADD KEY idx_promotion_quota_id (quota_id, status),
  ADD KEY idx_promotion_quota_business (business_type, business_id);

-- `reserved_quantity` remains the canonical quantity column for backward compatibility.
-- `reserved_amount` is retained for legacy amount-based promotion locks; new quota reservations
-- must use quota_id + reserved_quantity. quota_id is nullable only for migrated legacy rows.

CREATE TABLE coupon_template (
  id BIGINT PRIMARY KEY,
  template_code VARCHAR(64) NOT NULL,
  owner_type VARCHAR(32) NOT NULL,
  owner_id BIGINT NULL,
  coupon_type VARCHAR(32) NOT NULL,
  currency CHAR(3) NULL,
  claim_start_at DATETIME(3) NOT NULL,
  claim_end_at DATETIME(3) NOT NULL,
  use_start_at DATETIME(3) NOT NULL,
  use_end_at DATETIME(3) NOT NULL,
  threshold_amount DECIMAL(18,2) NULL,
  benefit_json JSON NOT NULL,
  scope_json JSON NOT NULL,
  exclusion_json JSON NULL,
  compatibility_policy_code VARCHAR(64) NOT NULL,
  funding_party VARCHAR(32) NOT NULL,
  total_issue_limit BIGINT NULL,
  per_user_claim_limit INT NULL,
  per_user_use_limit INT NULL,
  version_no INT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_coupon_template_version (template_code, version_no)
);

CREATE TABLE coupon_claim_reservation (
  id BIGINT PRIMARY KEY,
  reservation_no VARCHAR(64) NOT NULL,
  coupon_template_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  status VARCHAR(32) NOT NULL,
  expire_at DATETIME(3) NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_coupon_claim_res_no (reservation_no),
  UNIQUE KEY uk_coupon_claim_idem (idempotency_key),
  KEY idx_coupon_claim_user (coupon_template_id, user_id, status)
);

CREATE TABLE promotion_purchase_limit_counter (
  id BIGINT PRIMARY KEY,
  campaign_id BIGINT NOT NULL,
  limit_type VARCHAR(32) NOT NULL,
  subject_key VARCHAR(128) NOT NULL,
  sku_id BIGINT NULL,
  reserved_qty BIGINT NOT NULL DEFAULT 0,
  committed_qty BIGINT NOT NULL DEFAULT 0,
  released_qty BIGINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_purchase_limit_counter (campaign_id, limit_type, subject_key, sku_id)
);

CREATE TABLE flash_sale_sku (
  id BIGINT PRIMARY KEY,
  campaign_id BIGINT NOT NULL,
  offer_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  flash_price DECIMAL(18,2) NOT NULL,
  flash_quota_id BIGINT NOT NULL,
  per_user_limit INT NOT NULL,
  start_at DATETIME(3) NOT NULL,
  end_at DATETIME(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_flash_sale_sku (campaign_id, sku_id),
  KEY idx_flash_sale_active (sku_id, status, start_at, end_at)
);

CREATE TABLE flash_sale_reservation (
  id BIGINT PRIMARY KEY,
  reservation_no VARCHAR(64) NOT NULL,
  campaign_id BIGINT NOT NULL,
  flash_sale_sku_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(32) NOT NULL,
  trade_no VARCHAR(64) NULL,
  expire_at DATETIME(3) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_flash_reservation_no (reservation_no),
  UNIQUE KEY uk_flash_reservation_idem (idempotency_key),
  KEY idx_flash_reservation_expire (status, expire_at)
);

CREATE TABLE bundle_offer (
  id BIGINT PRIMARY KEY,
  bundle_offer_no VARCHAR(64) NOT NULL,
  merchant_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  title VARCHAR(256) NOT NULL,
  price_policy_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  version_no INT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_bundle_offer_version (bundle_offer_no, version_no)
);

CREATE TABLE bundle_offer_component (
  id BIGINT PRIMARY KEY,
  bundle_offer_id BIGINT NOT NULL,
  offer_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  required_flag TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_bundle_component (bundle_offer_id, sku_id)
);

CREATE TABLE promotion_calculation_snapshot (
  id BIGINT PRIMARY KEY,
  snapshot_no VARCHAR(64) NOT NULL,
  business_type VARCHAR(32) NOT NULL,
  business_id VARCHAR(128) NOT NULL,
  selected_candidates_json JSON NOT NULL,
  rejected_candidates_json JSON NULL,
  compatibility_policy_version VARCHAR(64) NOT NULL,
  benefit_total DECIMAL(18,2) NOT NULL,
  funding_allocation_json JSON NOT NULL,
  calculation_trace_json JSON NOT NULL,
  trace_hash VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_promotion_snapshot_no (snapshot_no),
  KEY idx_promotion_snapshot_business (business_type, business_id)
);
