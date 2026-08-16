package com.example.evcharging.system.auth;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.AccessPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth-api/v1")
public class AuthController {
    private final AuthService service;
    private final AuthSessionRevocationService revocations;

    public AuthController(AuthService service,AuthSessionRevocationService revocations){
        this.service=service;this.revocations=revocations;
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.LoginResult> login(@RequestBody AuthService.LoginRequest request){
        return ApiResponse.success(service.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthService.TokenPair> refresh(@RequestBody AuthService.RefreshRequest request){
        return ApiResponse.success(service.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String,String>> logout(HttpServletRequest request){
        long tenant=RequestContext.requireTenantId();
        Object sid=request.getAttribute("accessSessionId");
        if(sid==null) throw new SecurityException("access session missing");
        revocations.revokeSession(tenant,String.valueOf(sid),"USER_LOGOUT");
        return ApiResponse.success(Map.of("result","LOGGED_OUT"));
    }

    @PostMapping("/change-password")
    public ApiResponse<Map<String,String>> changePassword(@RequestBody AuthService.ChangePasswordRequest request){
        service.changePassword(RequestContext.requireTenantId(),RequestContext.requireUserId(),
                request.currentPassword(),request.newPassword());
        return ApiResponse.success(Map.of("result","PASSWORD_CHANGED"));
    }

    @GetMapping("/me")
    public ApiResponse<AccessPrincipal> me(){
        return ApiResponse.success(RequestContext.requirePrincipal());
    }
}
