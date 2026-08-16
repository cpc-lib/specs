ALTER TABLE finance_transaction_fact
  ADD COLUMN station_id BIGINT NULL AFTER biz_order_no,
  ADD KEY idx_finance_fact_station (tenant_id,station_id,business_date);

ALTER TABLE finance_refund_fact
  ADD COLUMN station_id BIGINT NULL AFTER payment_no,
  ADD KEY idx_finance_refund_station (tenant_id,station_id,business_date);

ALTER TABLE finance_settlement_source
  ADD COLUMN station_id BIGINT NULL AFTER biz_order_no,
  ADD KEY idx_settlement_source_station (tenant_id,station_id,business_date,status);
