ALTER TABLE operation_alarm
  ADD COLUMN station_id BIGINT NULL AFTER device_id,
  ADD KEY idx_alarm_station (tenant_id,station_id,status,last_occurred_time);

ALTER TABLE operation_work_order
  ADD COLUMN station_id BIGINT NULL AFTER alarm_no,
  ADD KEY idx_work_order_station (tenant_id,station_id,status,create_time);
