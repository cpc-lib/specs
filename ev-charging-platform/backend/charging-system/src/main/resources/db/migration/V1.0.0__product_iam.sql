CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  display_name VARCHAR(128) NOT NULL,
  password_hash VARCHAR(512) NOT NULL,
  status VARCHAR(32) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user (tenant_id,username)
);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(128) NOT NULL,
  data_scope_type VARCHAR(32) NOT NULL,
  create_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role (tenant_id,role_code)
);

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  permission_name VARCHAR(128) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (permission_code)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id,role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id,permission_id)
);

CREATE TABLE IF NOT EXISTS sys_user_station_scope (
  user_id BIGINT NOT NULL,
  station_id BIGINT NOT NULL,
  PRIMARY KEY (user_id,station_id)
);

CREATE TABLE IF NOT EXISTS sys_login_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT,
  user_id BIGINT,
  username VARCHAR(64),
  success BIT NOT NULL,
  failure_reason VARCHAR(255),
  login_time DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_login_user_time (tenant_id,user_id,login_time)
);
