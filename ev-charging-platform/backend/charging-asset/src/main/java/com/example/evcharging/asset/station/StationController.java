package com.example.evcharging.asset.station;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api/v1/assets/stations")
public class StationController {
    private final StationApplicationService service;

    public StationController(StationApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<StationEntity> create(@Valid @RequestBody CreateStationRequest request) {
        return ApiResponse.success(service.create(RequestContext.requireTenantId(), request));
    }

    @GetMapping
    public ApiResponse<List<StationEntity>> list() {
        return ApiResponse.success(service.list(RequestContext.requireTenantId()));
    }
}
