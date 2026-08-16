package com.example.evcharging.asset.charger;
import com.example.evcharging.asset.station.StationApplicationService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/internal-api/v1/assets")
public class AssetInternalController {
  private final AssetDeviceApplicationService service; private final StationApplicationService stations; public AssetInternalController(AssetDeviceApplicationService service,StationApplicationService stations){this.service=service;this.stations=stations;}
  @GetMapping("/connectors/{connectorCode}") public ConnectorSnapshot connector(@PathVariable String connectorCode,@RequestParam long tenantId){return service.snapshot(tenantId,connectorCode);}
  @GetMapping("/stations/{stationId}/exists") public boolean stationExists(@PathVariable long stationId,@RequestParam long tenantId){return stations.exists(tenantId,stationId);}
}
