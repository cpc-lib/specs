ALTER TABLE payment_order
  ADD COLUMN station_id BIGINT NULL AFTER biz_order_no,
  ADD KEY idx_payment_station (tenant_id,station_id,create_time);

ALTER TABLE payment_refund
  ADD COLUMN station_id BIGINT NULL AFTER payment_no,
  ADD KEY idx_refund_station (tenant_id,station_id,create_time);
