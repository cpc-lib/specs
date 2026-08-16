package com.example.evcharging.core.asset;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="charging-asset",contextId="assetConnectorClient")
public interface AssetConnectorClient {
  @GetMapping("/internal-api/v1/assets/connectors/{connectorCode}") ConnectorSnapshot find(@PathVariable String connectorCode,@RequestParam long tenantId);
}
