ALTER TABLE charger
  ADD UNIQUE KEY uk_tenant_device_sn (tenant_id, device_sn);

CREATE INDEX idx_connector_tenant_code_status
  ON charger_connector(tenant_id, connector_code, running_status);
