package com.example.evcharging.core.billing.application;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api/v1/billing")
public class BillingAdminController {
    private final BillingAdminService service;
    private final BillingReplayService replay;
    public BillingAdminController(BillingAdminService service, BillingReplayService replay) { this.service = service; this.replay = replay; }

    @PostMapping("/stations/{stationId}/versions")
    public ApiResponse<BillingSnapshotDocument> publish(@PathVariable long stationId, @Valid @RequestBody PublishBillingVersionRequest request) {
        return ApiResponse.success(service.publish(RequestContext.requireTenantId(), stationId, request));
    }

    @GetMapping("/stations/{stationId}/current")
    public ApiResponse<BillingSnapshotDocument> current(@PathVariable long stationId) {
        return ApiResponse.success(service.current(RequestContext.requireTenantId(), stationId));
    }

    @PostMapping("/sessions/{sessionNo}/replay")
    public ApiResponse<BillingReplayService.ReplayResult> replay(@PathVariable String sessionNo) throws Exception {
        return ApiResponse.success(replay.replay(RequestContext.requireTenantId(), sessionNo));
    }
}
