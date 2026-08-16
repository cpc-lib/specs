package com.example.evcharging.operation.asset;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="charging-asset",path="/internal-api/v1/assets/devices")
public interface AssetDeviceContextClient {
    @GetMapping("/{deviceId}/context")
    DeviceContext context(@PathVariable String deviceId);

    record DeviceContext(long chargerId,long stationId,String chargerCode,String deviceSn){}
}
