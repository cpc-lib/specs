package com.example.evcharging.open.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@FeignClient(name="charging-asset",path="/internal-api/v1/assets/open")
public interface AssetOpenClient {
    @PostMapping("/stations/query")
    List<StationView> stations(@RequestBody StationQuery query);

    @GetMapping("/connectors/{connectorCode}/context")
    ConnectorContext connector(@PathVariable String connectorCode);

    @GetMapping("/stations/{stationId}")
    StationDetail detail(@PathVariable long stationId);

    record ConnectorContext(long connectorId,long stationId,long chargerId,String connectorCode,int onlineStatus,int runningStatus){}
    record StationQuery(boolean allStations,Set<Long> stationIds){}
    record StationView(long stationId,String stationCode,String stationName,Double longitude,Double latitude,
                       int connectorCount,int availableConnectors){}
    record ConnectorView(long connectorId,String connectorCode,int connectorNo,int connectorType,Long ratedPowerW,
                         int onlineStatus,int runningStatus,String chargerCode){}
    record StationDetail(StationView station,List<ConnectorView> connectors){}
}
