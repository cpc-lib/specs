package com.example.evcharging.system.admin;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin-api/v1/system")
@RequirePermission("system:manage")
public class SystemAdminController {
    private final SystemAdminService service;
    public SystemAdminController(SystemAdminService service){this.service=service;}

    @GetMapping("/users")
    public ApiResponse<List<SystemAdminService.UserView>> users(){return ApiResponse.success(service.users());}

    @PostMapping("/users")
    public ApiResponse<Map<String,Long>> createUser(@RequestBody SystemAdminService.CreateUserCommand request){
        return ApiResponse.success(Map.of("userId",service.createUser(request)));
    }

    @PutMapping("/users/{userId}/roles")
    public ApiResponse<Map<String,String>> roles(@PathVariable long userId,@RequestBody RoleAssignment request){
        service.replaceUserRoles(userId,request.roleCodes());return ApiResponse.success(Map.of("result","UPDATED"));
    }

    @GetMapping("/users/{userId}/station-scope")
    public ApiResponse<Set<Long>> stationScope(@PathVariable long userId){return ApiResponse.success(service.stationScope(userId));}

    @PutMapping("/users/{userId}/station-scope")
    public ApiResponse<Map<String,String>> stationScope(@PathVariable long userId,@RequestBody StationScopeAssignment request){
        service.replaceStationScope(userId,request.stationIds());return ApiResponse.success(Map.of("result","UPDATED"));
    }

    @PostMapping("/users/{userId}/reset-password")
    public ApiResponse<Map<String,String>> resetPassword(@PathVariable long userId,@RequestBody PasswordReset request){
        service.resetPassword(userId,request.newPassword());return ApiResponse.success(Map.of("result","PASSWORD_RESET"));
    }

    @PutMapping("/users/{userId}/status")
    public ApiResponse<Map<String,String>> status(@PathVariable long userId,@RequestBody UserStatus request){
        service.setStatus(userId,request.status());return ApiResponse.success(Map.of("result","UPDATED"));
    }

    @GetMapping("/roles")
    public ApiResponse<List<SystemAdminService.RoleView>> roles(){return ApiResponse.success(service.roles());}

    @PostMapping("/roles")
    public ApiResponse<Map<String,Long>> createRole(@RequestBody SystemAdminService.CreateRoleCommand request){
        return ApiResponse.success(Map.of("roleId",service.createRole(request)));
    }

    @PutMapping("/roles/{roleCode}")
    public ApiResponse<Map<String,String>> updateRole(@PathVariable String roleCode,@RequestBody SystemAdminService.UpdateRoleCommand request){
        service.updateRole(roleCode,request);return ApiResponse.success(Map.of("result","UPDATED"));
    }

    @DeleteMapping("/roles/{roleCode}")
    public ApiResponse<Map<String,String>> deleteRole(@PathVariable String roleCode){
        service.deleteRole(roleCode);return ApiResponse.success(Map.of("result","DELETED"));
    }

    @GetMapping("/permissions")
    public ApiResponse<List<SystemAdminService.PermissionView>> permissions(){return ApiResponse.success(service.permissions());}

    @PostMapping("/permissions")
    public ApiResponse<Map<String,Long>> createPermission(@RequestBody SystemAdminService.CreatePermissionCommand request){
        return ApiResponse.success(Map.of("permissionId",service.createPermission(request)));
    }

    @PutMapping("/permissions/{permissionCode}")
    public ApiResponse<Map<String,String>> updatePermission(@PathVariable String permissionCode,@RequestBody SystemAdminService.UpdatePermissionCommand request){
        service.updatePermission(permissionCode,request);return ApiResponse.success(Map.of("result","UPDATED"));
    }

    @DeleteMapping("/permissions/{permissionCode}")
    public ApiResponse<Map<String,String>> deletePermission(@PathVariable String permissionCode){
        service.deletePermission(permissionCode);return ApiResponse.success(Map.of("result","DELETED"));
    }

    public record RoleAssignment(Set<String> roleCodes){}
    public record StationScopeAssignment(Set<Long> stationIds){}
    public record PasswordReset(String newPassword){}
    public record UserStatus(String status){}
}
