package com.example.evcharging.open.admin;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/open/ops")
@RequirePermission("open:manage")
public class OpenObservabilityAdminController {
    private final OpenObservabilityService service;
    public OpenObservabilityAdminController(OpenObservabilityService service){this.service=service;}

    @GetMapping("/audits")
    public ApiResponse<List<OpenObservabilityService.AuditView>> audits(@RequestParam(defaultValue="100") int limit){
        return ApiResponse.success(service.audits(limit));
    }
    @GetMapping("/callbacks")
    public ApiResponse<List<OpenObservabilityService.CallbackTaskView>> callbacks(@RequestParam(defaultValue="100") int limit){
        return ApiResponse.success(service.callbacks(limit));
    }
    @PostMapping("/callbacks/{id}/retry")
    public ApiResponse<Map<String,String>> retry(@PathVariable long id){
        service.retryCallback(id);return ApiResponse.success(Map.of("result","RETRY"));
    }
}
