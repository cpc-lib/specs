CREATE TABLE category_attribute_definition (
  id BIGINT PRIMARY KEY,
  category_id BIGINT NOT NULL,
  attribute_code VARCHAR(64) NOT NULL,
  attribute_name VARCHAR(128) NOT NULL,
  attribute_type VARCHAR(32) NOT NULL,
  data_type VARCHAR(32) NOT NULL,
  required_flag TINYINT NOT NULL DEFAULT 0,
  searchable_flag TINYINT NOT NULL DEFAULT 0,
  filterable_flag TINYINT NOT NULL DEFAULT 0,
  sku_dimension_flag TINYINT NOT NULL DEFAULT 0,
  unit_code VARCHAR(32) NULL,
  validation_json JSON NULL,
  option_source VARCHAR(32) NULL,
  display_order INT NOT NULL DEFAULT 0,
  version_no INT NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  status VARCHAR(32) NOT NULL,
  UNIQUE KEY uk_category_attr_version (category_id, attribute_code, version_no),
  KEY idx_category_attr_active (category_id, status, effective_from, effective_to)
);

CREATE TABLE category_attribute_option (
  id BIGINT PRIMARY KEY,
  attribute_definition_id BIGINT NOT NULL,
  option_code VARCHAR(64) NOT NULL,
  option_value VARCHAR(256) NOT NULL,
  normalized_value VARCHAR(256) NOT NULL,
  display_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_attr_option (attribute_definition_id, option_code)
);

CREATE TABLE product_spu_version (
  id BIGINT PRIMARY KEY,
  spu_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  category_id BIGINT NOT NULL,
  brand_id BIGINT NULL,
  title VARCHAR(512) NOT NULL,
  description_ref VARCHAR(512) NULL,
  attribute_schema_version INT NOT NULL,
  attribute_json JSON NOT NULL,
  media_json JSON NULL,
  compliance_snapshot_id BIGINT NULL,
  status VARCHAR(32) NOT NULL,
  content_hash VARCHAR(128) NOT NULL,
  created_by BIGINT NULL,
  created_at DATETIME(3) NOT NULL,
  published_at DATETIME(3) NULL,
  UNIQUE KEY uk_spu_version (spu_id, version_no),
  KEY idx_spu_version_status (spu_id, status)
);

CREATE TABLE product_sku_version (
  id BIGINT PRIMARY KEY,
  sku_id BIGINT NOT NULL,
  spu_version_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  normalized_spec_key VARCHAR(1024) NOT NULL,
  spec_json JSON NOT NULL,
  barcode VARCHAR(128) NULL,
  weight_grams INT NULL,
  volume_cm3 BIGINT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_sku_version (sku_id, version_no),
  KEY idx_sku_version_spu (spu_version_id, status)
);

CREATE TABLE merchant_offer_version (
  id BIGINT PRIMARY KEY,
  offer_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  merchant_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  spu_version_id BIGINT NOT NULL,
  sku_version_id BIGINT NOT NULL,
  seller_title VARCHAR(512) NOT NULL,
  main_image_file_id BIGINT NULL,
  media_json JSON NULL,
  sale_policy_json JSON NOT NULL,
  aftersale_policy_json JSON NOT NULL,
  logistics_policy_json JSON NOT NULL,
  region_policy_json JSON NULL,
  compliance_snapshot_id BIGINT NULL,
  content_hash VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  published_at DATETIME(3) NULL,
  UNIQUE KEY uk_offer_version (offer_id, version_no),
  KEY idx_offer_version_active (offer_id, status)
);

CREATE TABLE brand_authorization (
  id BIGINT PRIMARY KEY,
  authorization_no VARCHAR(64) NOT NULL,
  merchant_id BIGINT NOT NULL,
  shop_id BIGINT NULL,
  brand_id BIGINT NOT NULL,
  category_id BIGINT NULL,
  authorization_type VARCHAR(32) NOT NULL,
  proof_file_ids_json JSON NOT NULL,
  issuer VARCHAR(256) NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  status VARCHAR(32) NOT NULL,
  workflow_instance_id VARCHAR(128) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_brand_authorization_no (authorization_no),
  KEY idx_brand_auth_active (merchant_id, brand_id, category_id, status, effective_from, effective_to)
);

CREATE TABLE product_compliance_snapshot (
  id BIGINT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  offer_id BIGINT NULL,
  spu_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  requirement_version VARCHAR(64) NOT NULL,
  evidence_json JSON NOT NULL,
  result VARCHAR(32) NOT NULL,
  expire_at DATETIME(3) NULL,
  snapshot_hash VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_compliance_offer (offer_id, result, expire_at)
);

CREATE TABLE offer_publish_request (
  id BIGINT PRIMARY KEY,
  request_no VARCHAR(64) NOT NULL,
  offer_id BIGINT NOT NULL,
  offer_version_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  validation_snapshot_json JSON NULL,
  moderation_result_ref VARCHAR(128) NULL,
  workflow_instance_id VARCHAR(128) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_offer_publish_request_no (request_no),
  KEY idx_offer_publish_offer (offer_id, status)
);
