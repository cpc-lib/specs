CREATE TABLE recommendation_model_version (
  id BIGINT PRIMARY KEY,
  model_code VARCHAR(64) NOT NULL,
  version_no VARCHAR(64) NOT NULL,
  scene VARCHAR(64) NOT NULL,
  artifact_ref VARCHAR(512) NULL,
  feature_schema_version VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_recommend_model_version (model_code, version_no)
);

CREATE TABLE recommendation_policy (
  id BIGINT PRIMARY KEY,
  policy_code VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  scene VARCHAR(64) NOT NULL,
  candidate_source_json JSON NOT NULL,
  rank_config_json JSON NOT NULL,
  diversity_config_json JSON NULL,
  filter_config_json JSON NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  UNIQUE KEY uk_recommend_policy (policy_code, version_no)
);

CREATE TABLE recommendation_experiment (
  id BIGINT PRIMARY KEY,
  experiment_code VARCHAR(64) NOT NULL,
  scene VARCHAR(64) NOT NULL,
  subject_type VARCHAR(32) NOT NULL,
  traffic_allocation DECIMAL(8,6) NOT NULL,
  status VARCHAR(32) NOT NULL,
  start_at DATETIME(3) NOT NULL,
  end_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_recommend_experiment_code (experiment_code)
);

CREATE TABLE recommendation_experiment_variant (
  id BIGINT PRIMARY KEY,
  experiment_id BIGINT NOT NULL,
  variant_code VARCHAR(64) NOT NULL,
  allocation DECIMAL(8,6) NOT NULL,
  model_version_ref VARCHAR(128) NULL,
  policy_version_ref VARCHAR(128) NULL,
  config_json JSON NULL,
  UNIQUE KEY uk_recommend_variant (experiment_id, variant_code)
);

CREATE TABLE recommendation_experiment_assignment (
  id BIGINT PRIMARY KEY,
  experiment_id BIGINT NOT NULL,
  subject_hash VARCHAR(128) NOT NULL,
  variant_code VARCHAR(64) NOT NULL,
  assigned_at DATETIME(3) NOT NULL,
  expire_at DATETIME(3) NULL,
  UNIQUE KEY uk_recommend_assignment (experiment_id, subject_hash)
);
