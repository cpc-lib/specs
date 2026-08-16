package com.example.evcharging.open.partner;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.open.integration.*;
import com.example.evcharging.open.security.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/open-api/v1")
public class OpenPartnerController {
    private final AssetOpenClient assets;private final PartnerChargingService charging;
    public OpenPartnerController(AssetOpenClient assets,PartnerChargingService charging){this.assets=assets;this.charging=charging;}

    @GetMapping("/stations")
    public ApiResponse<List<AssetOpenClient.StationView>> stations(){
        PartnerScopeGuard.require("station:read");
        var p=PartnerContext.require();
        boolean all="ALL".equalsIgnoreCase(p.dataScopeType());
        return ApiResponse.success(assets.stations(new AssetOpenClient.StationQuery(all,p.stationIds())));
    }

    @GetMapping("/stations/{stationId}")
    public ApiResponse<AssetOpenClient.StationDetail> station(@PathVariable long stationId){
        PartnerScopeGuard.require("station:read");PartnerScopeGuard.requireStation(stationId);
        return ApiResponse.success(assets.detail(stationId));
    }

    @PostMapping("/charging/start")
    public ApiResponse<PartnerChargingService.StartResult> start(@RequestBody PartnerChargingService.StartRequest request){
        return ApiResponse.success(charging.start(request));
    }

    @PostMapping("/charging/{sessionNo}/stop")
    public ApiResponse<CorePartnerClient.SessionView> stop(@PathVariable String sessionNo,@RequestBody PartnerChargingService.StopRequest request){
        return ApiResponse.success(charging.stop(sessionNo,request));
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<CorePartnerClient.OrderSnapshot> order(@PathVariable String orderNo){
        return ApiResponse.success(charging.order(orderNo));
    }
}
