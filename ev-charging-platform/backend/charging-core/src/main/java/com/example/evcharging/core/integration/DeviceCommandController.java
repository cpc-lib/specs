package com.example.evcharging.core.integration;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.contract.DeviceCommandMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin-api/v1/charging/device-commands")
public class DeviceCommandController {
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    public DeviceCommandController(RabbitTemplate rabbit, ObjectMapper mapper) {
        this.rabbit = rabbit;
        this.mapper = mapper;
    }

    @PostMapping("/test")
    public ApiResponse<Map<String, String>> test(@RequestParam String deviceId) {
        long tenantId = RequestContext.requireTenantId();
        String commandId = UUID.randomUUID().toString();
        DeviceCommandMessage command = new DeviceCommandMessage(
                tenantId, commandId, deviceId, "QUERY_STATUS", Instant.now().plusSeconds(30), Map.of());
        try {
            rabbit.convertAndSend(
                    "ev.device.command.exchange",
                    "gateway.dev",
                    mapper.writeValueAsString(command));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cannot serialize device command", e);
        }
        return ApiResponse.success(Map.of("commandId", commandId, "status", "ACCEPTED"));
    }
}
