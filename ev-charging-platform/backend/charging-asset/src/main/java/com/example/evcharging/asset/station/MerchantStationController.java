package com.example.evcharging.asset.station;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.DataScopeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/merchant-api/v1/assets/stations")
public class MerchantStationController {
    private final StationApplicationService service;

    public MerchantStationController(StationApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<StationEntity>> list() {
        var principal=RequestContext.requirePrincipal();
        boolean tenantWide=principal.dataScopeType()==DataScopeType.ALL||principal.dataScopeType()==DataScopeType.TENANT;
        return ApiResponse.success(service.listScoped(principal.tenantId(),principal.stationIds(),tenantWide));
    }
}
