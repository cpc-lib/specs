package com.example.evcharging.asset.charger;
import com.example.evcharging.framework.api.ApiResponse; import com.example.evcharging.framework.context.RequestContext; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/admin-api/v1/assets")
public class AssetDeviceController {
  private final AssetDeviceApplicationService service; public AssetDeviceController(AssetDeviceApplicationService service){this.service=service;}
  @PostMapping("/stations/{stationId}/chargers") public ApiResponse<ChargerEntity> createCharger(@PathVariable long stationId,@Valid @RequestBody CreateChargerRequest r){return ApiResponse.success(service.createCharger(RequestContext.requireTenantId(),stationId,r));}
  @GetMapping("/stations/{stationId}/chargers") public ApiResponse<List<ChargerEntity>> listChargers(@PathVariable long stationId){return ApiResponse.success(service.listChargers(RequestContext.requireTenantId(),stationId));}
  @PostMapping("/chargers/{chargerId}/connectors") public ApiResponse<ConnectorEntity> createConnector(@PathVariable long chargerId,@Valid @RequestBody CreateConnectorRequest r){return ApiResponse.success(service.createConnector(RequestContext.requireTenantId(),chargerId,r));}
  @GetMapping("/chargers/{chargerId}/connectors") public ApiResponse<List<ConnectorEntity>> listConnectors(@PathVariable long chargerId){return ApiResponse.success(service.listConnectors(RequestContext.requireTenantId(),chargerId));}
}
