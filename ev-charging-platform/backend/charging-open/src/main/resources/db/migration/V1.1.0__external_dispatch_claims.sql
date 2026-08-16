ALTER TABLE open_partner_callback_task
  ADD COLUMN claim_token VARCHAR(64) NULL AFTER retry_count,
  ADD COLUMN claim_time DATETIME(3) NULL AFTER claim_token;

ALTER TABLE open_regulatory_report_task
  ADD COLUMN claim_token VARCHAR(64) NULL AFTER retry_count,
  ADD COLUMN claim_time DATETIME(3) NULL AFTER claim_token;
