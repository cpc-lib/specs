CREATE TABLE risk_rule (
 id BIGINT PRIMARY KEY,
 rule_code VARCHAR(64) NOT NULL,
 scenario VARCHAR(64) NOT NULL,
 version_no INT NOT NULL,
 rule_json JSON NOT NULL,
 effective_from DATETIME(3) NOT NULL,
 effective_to DATETIME(3),
 status VARCHAR(32) NOT NULL,
 UNIQUE KEY uk_risk_rule_version (rule_code, version_no)
);
CREATE TABLE risk_decision_log (
 id BIGINT PRIMARY KEY,
 scenario VARCHAR(64) NOT NULL,
 subject_type VARCHAR(32) NOT NULL,
 subject_id VARCHAR(128) NOT NULL,
 decision VARCHAR(32) NOT NULL,
 score DECIMAL(10,4),
 rule_version_snapshot JSON,
 created_at DATETIME(3) NOT NULL,
 KEY idx_risk_subject (subject_type, subject_id, created_at)
);
