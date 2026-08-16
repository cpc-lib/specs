package com.example.evcharging.iot.api;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.contract.DeviceRouteLease;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin-api/v1/iot/devices")
public class DeviceStatusController {
    private final StringRedisTemplate redis;

    public DeviceStatusController(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @GetMapping("/{deviceId}/status")
    public ApiResponse<Map<String, Object>> status(@PathVariable String deviceId) {
        long tenantId = RequestContext.requireTenantId();
        String key = "ev:" + tenantId + ":device:online:" + deviceId;
        boolean online = Boolean.TRUE.equals(redis.hasKey(key));
        String route = redis.opsForValue().get("ev:" + tenantId + ":device:route:" + deviceId);
        String gatewayId = route == null ? null : DeviceRouteLease.parse(route).gatewayId();
        Map<String,Object> result = new java.util.LinkedHashMap<>();
        result.put("deviceId", deviceId);
        result.put("online", online);
        result.put("gatewayId", gatewayId);
        return ApiResponse.success(result);
    }
}
