package com.example.evcharging.core.asset;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name="charging-asset",contextId="assetStationClient")
public interface AssetStationClient {
  @GetMapping("/internal-api/v1/assets/stations/{stationId}/exists") boolean exists(@PathVariable long stationId,@RequestParam long tenantId);
}
