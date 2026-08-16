CREATE TABLE price_book (
  id BIGINT PRIMARY KEY,
  price_book_code VARCHAR(64) NOT NULL,
  owner_type VARCHAR(32) NOT NULL,
  merchant_id BIGINT NULL,
  shop_id BIGINT NULL,
  currency CHAR(3) NOT NULL,
  dimension_type VARCHAR(32) NOT NULL,
  dimension_value VARCHAR(128) NULL,
  priority INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  version_no INT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_price_book_version (price_book_code, version_no),
  KEY idx_price_book_active (status, effective_from, effective_to, priority)
);

CREATE TABLE price_book_item (
  id BIGINT PRIMARY KEY,
  price_book_id BIGINT NOT NULL,
  offer_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  price DECIMAL(18,2) NOT NULL,
  floor_price DECIMAL(18,2) NULL,
  compare_at_price DECIMAL(18,2) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_price_book_offer (price_book_id, offer_id),
  KEY idx_price_item_offer (offer_id, price_book_id)
);

CREATE TABLE price_selection_policy (
  id BIGINT PRIMARY KEY,
  policy_code VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  precedence_json JSON NOT NULL,
  tie_break_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  UNIQUE KEY uk_price_selection_policy (policy_code, version_no)
);

CREATE TABLE price_change_request (
  id BIGINT PRIMARY KEY,
  request_no VARCHAR(64) NOT NULL,
  offer_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  old_price DECIMAL(18,2) NULL,
  new_price DECIMAL(18,2) NOT NULL,
  reason VARCHAR(1024) NULL,
  risk_flags_json JSON NULL,
  status VARCHAR(32) NOT NULL,
  workflow_instance_id VARCHAR(128) NULL,
  scheduled_effective_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_price_change_request_no (request_no),
  KEY idx_price_change_offer (offer_id, status)
);

CREATE TABLE pricing_calculation_snapshot (
  id BIGINT PRIMARY KEY,
  snapshot_no VARCHAR(64) NOT NULL,
  business_type VARCHAR(32) NOT NULL,
  business_id VARCHAR(128) NOT NULL,
  buyer_id BIGINT NULL,
  offer_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  currency CHAR(3) NOT NULL,
  base_price DECIMAL(18,2) NOT NULL,
  selected_unit_price DECIMAL(18,2) NOT NULL,
  selected_price_source VARCHAR(32) NOT NULL,
  selected_price_ref BIGINT NULL,
  policy_version VARCHAR(64) NOT NULL,
  context_json JSON NOT NULL,
  calculation_trace_json JSON NOT NULL,
  trace_hash VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_pricing_snapshot_no (snapshot_no),
  KEY idx_pricing_snapshot_business (business_type, business_id)
);
