package com.example.evcharging.open.admin;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/open/partners")
@RequirePermission("open:manage")
public class OpenPartnerAdminController {
    private final PartnerManagementService service;
    public OpenPartnerAdminController(PartnerManagementService service){this.service=service;}

    @GetMapping public ApiResponse<List<PartnerManagementService.PartnerView>> list(){return ApiResponse.success(service.list());}
    @GetMapping("/{id}") public ApiResponse<PartnerManagementService.PartnerDetail> detail(@PathVariable long id){return ApiResponse.success(service.detail(id));}
    @PostMapping public ApiResponse<PartnerManagementService.CreatedPartner> create(@RequestBody PartnerManagementService.CreatePartnerCommand c){return ApiResponse.success(service.create(c));}
    @PutMapping("/{id}/access") public ApiResponse<Map<String,String>> access(@PathVariable long id,@RequestBody PartnerManagementService.UpdateAccessCommand c){
        service.updateAccess(id,c);return ApiResponse.success(Map.of("result","UPDATED"));
    }
    @PostMapping("/{id}/rotate-secret") public ApiResponse<PartnerManagementService.RotatedSecret> rotate(@PathVariable long id){return ApiResponse.success(service.rotateSecret(id));}
    @PostMapping("/{id}/rotate-callback-secret") public ApiResponse<PartnerManagementService.RotatedSecret> rotateCallback(@PathVariable long id){return ApiResponse.success(service.rotateCallbackSecret(id));}
}
