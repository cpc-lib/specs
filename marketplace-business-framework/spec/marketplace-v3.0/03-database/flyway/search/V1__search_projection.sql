CREATE TABLE search_projection_checkpoint (
  id BIGINT PRIMARY KEY,
  projection_name VARCHAR(128) NOT NULL,
  partition_key VARCHAR(128) NOT NULL,
  last_event_id VARCHAR(64) NULL,
  last_source_version BIGINT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_search_projection_checkpoint (projection_name, partition_key)
);

CREATE TABLE search_reindex_job (
  id BIGINT PRIMARY KEY,
  job_no VARCHAR(64) NOT NULL,
  index_type VARCHAR(32) NOT NULL,
  source_version VARCHAR(64) NOT NULL,
  target_index_name VARCHAR(256) NOT NULL,
  status VARCHAR(32) NOT NULL,
  processed_count BIGINT NOT NULL DEFAULT 0,
  failed_count BIGINT NOT NULL DEFAULT 0,
  started_at DATETIME(3) NULL,
  finished_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_search_reindex_job_no (job_no),
  KEY idx_search_reindex_status (status, created_at)
);

CREATE TABLE search_synonym_set (
  id BIGINT PRIMARY KEY,
  synonym_set_code VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  locale VARCHAR(32) NOT NULL,
  synonym_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  UNIQUE KEY uk_search_synonym_version (synonym_set_code, version_no)
);

CREATE TABLE search_query_rewrite_rule (
  id BIGINT PRIMARY KEY,
  rule_code VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  locale VARCHAR(32) NOT NULL,
  condition_json JSON NOT NULL,
  rewrite_json JSON NOT NULL,
  priority INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  UNIQUE KEY uk_search_rewrite_rule (rule_code, version_no)
);

CREATE TABLE search_rank_policy (
  id BIGINT PRIMARY KEY,
  policy_code VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  scene VARCHAR(64) NOT NULL,
  feature_weight_json JSON NOT NULL,
  constraint_json JSON NULL,
  status VARCHAR(32) NOT NULL,
  effective_from DATETIME(3) NOT NULL,
  effective_to DATETIME(3) NULL,
  UNIQUE KEY uk_search_rank_policy (policy_code, version_no)
);
