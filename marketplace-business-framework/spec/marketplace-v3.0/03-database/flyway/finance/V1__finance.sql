CREATE TABLE marketplace_ledger_entry (
 id BIGINT PRIMARY KEY,
 entry_no VARCHAR(64) NOT NULL,
 business_type VARCHAR(32) NOT NULL,
 business_id VARCHAR(128) NOT NULL,
 currency CHAR(3) NOT NULL,
 occurred_at DATETIME(3) NOT NULL,
 created_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_ledger_entry_no (entry_no),
 KEY idx_ledger_business (business_type, business_id)
);
CREATE TABLE marketplace_ledger_line (
 id BIGINT PRIMARY KEY,
 entry_id BIGINT NOT NULL,
 account_code VARCHAR(64) NOT NULL,
 direction VARCHAR(8) NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 merchant_id BIGINT,
 buyer_id BIGINT,
 KEY idx_ledger_line_entry (entry_id),
 KEY idx_ledger_merchant (merchant_id, account_code)
);
