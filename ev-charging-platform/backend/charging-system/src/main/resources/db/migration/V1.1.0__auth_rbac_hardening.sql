CREATE TABLE IF NOT EXISTS sys_auth_session (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  session_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  refresh_token_hash CHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_time DATETIME(3) NOT NULL,
  last_rotated_time DATETIME(3) NOT NULL,
  expires_time DATETIME(3) NOT NULL,
  revoked_time DATETIME(3),
  revoke_reason VARCHAR(128),
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_session_id (session_id),
  UNIQUE KEY uk_refresh_token_hash (refresh_token_hash),
  KEY idx_auth_session_user (tenant_id,user_id,status,expires_time)
);

ALTER TABLE sys_role
  ADD COLUMN update_time DATETIME(3) NULL AFTER create_time;

ALTER TABLE sys_user
  ADD COLUMN password_changed_time DATETIME(3) NULL AFTER password_hash,
  ADD COLUMN failed_login_count INT NOT NULL DEFAULT 0 AFTER status,
  ADD COLUMN locked_until DATETIME(3) NULL AFTER failed_login_count;

CREATE TABLE IF NOT EXISTS sys_security_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  actor_user_id BIGINT,
  target_user_id BIGINT,
  event_type VARCHAR(64) NOT NULL,
  detail_json JSON,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_security_audit (tenant_id,event_type,create_time),
  KEY idx_security_target (tenant_id,target_user_id,create_time)
);
