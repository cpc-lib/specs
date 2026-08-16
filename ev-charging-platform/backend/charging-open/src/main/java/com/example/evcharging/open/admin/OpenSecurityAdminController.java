package com.example.evcharging.open.admin;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api/v1/open/security")
@RequirePermission("open:manage")
public class OpenSecurityAdminController {
    private final OpenSecretRewrapService service;
    public OpenSecurityAdminController(OpenSecretRewrapService service){this.service=service;}

    @PostMapping("/rewrap-secrets")
    public ApiResponse<OpenSecretRewrapService.Result> rewrap(){return ApiResponse.success(service.rewrapTenantSecrets());}
}
