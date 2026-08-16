CREATE TABLE IF NOT EXISTS finance_event_inbox (
  id BIGINT NOT NULL,event_id VARCHAR(64) NOT NULL,event_type VARCHAR(128) NOT NULL,processed_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id),UNIQUE KEY uk_finance_event(event_id)
);
CREATE TABLE IF NOT EXISTS finance_ledger_account (
  id BIGINT NOT NULL,tenant_id BIGINT NOT NULL,account_code VARCHAR(64) NOT NULL,account_name VARCHAR(128) NOT NULL,normal_side VARCHAR(8) NOT NULL,status VARCHAR(16) NOT NULL,create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id),UNIQUE KEY uk_ledger_account(tenant_id,account_code)
);
CREATE TABLE IF NOT EXISTS finance_ledger_transaction (
  id BIGINT NOT NULL,tenant_id BIGINT NOT NULL,transaction_no VARCHAR(64) NOT NULL,biz_event_id VARCHAR(64) NOT NULL,biz_type VARCHAR(64) NOT NULL,biz_no VARCHAR(64) NOT NULL,currency CHAR(3) NOT NULL,total_debit_fen BIGINT NOT NULL,total_credit_fen BIGINT NOT NULL,occurred_time DATETIME(3) NOT NULL,create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id),UNIQUE KEY uk_ledger_tx_no(transaction_no),UNIQUE KEY uk_ledger_event(tenant_id,biz_event_id)
);
CREATE TABLE IF NOT EXISTS finance_ledger_entry (
  id BIGINT NOT NULL,tenant_id BIGINT NOT NULL,transaction_id BIGINT NOT NULL,account_code VARCHAR(64) NOT NULL,entry_side VARCHAR(8) NOT NULL,amount_fen BIGINT NOT NULL,currency CHAR(3) NOT NULL,memo VARCHAR(255),create_time DATETIME(3) NOT NULL,
  PRIMARY KEY(id),KEY idx_ledger_entry_tx(transaction_id),KEY idx_ledger_account_time(tenant_id,account_code,create_time)
);
