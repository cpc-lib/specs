package com.example.evcharging.open.admin;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/open/regulatory")
@RequirePermission("regulatory:manage")
public class RegulatoryAdminController {
    private final RegulatoryManagementService service;
    public RegulatoryAdminController(RegulatoryManagementService service){this.service=service;}

    @GetMapping("/platforms")
    public ApiResponse<List<RegulatoryManagementService.PlatformView>> platforms(){return ApiResponse.success(service.list());}
    @PostMapping("/platforms")
    public ApiResponse<Map<String,Long>> create(@RequestBody RegulatoryManagementService.CreatePlatformCommand c){
        return ApiResponse.success(Map.of("platformId",service.create(c)));
    }
    @PutMapping("/platforms/{id}")
    public ApiResponse<Map<String,String>> update(@PathVariable long id,@RequestBody RegulatoryManagementService.UpdatePlatformCommand c){
        service.update(id,c);return ApiResponse.success(Map.of("result","UPDATED"));
    }
    @GetMapping("/tasks")
    public ApiResponse<List<RegulatoryManagementService.TaskView>> tasks(@RequestParam(defaultValue="100") int limit){
        return ApiResponse.success(service.tasks(limit));
    }
    @PostMapping("/tasks/{id}/retry")
    public ApiResponse<Map<String,String>> retry(@PathVariable long id){
        service.retry(id);return ApiResponse.success(Map.of("result","RETRY"));
    }
}
