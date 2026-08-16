package com.example.evcharging.core.charging.application;
import com.example.evcharging.framework.api.ApiResponse; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/app-api/v1/charging/sessions")
public class ChargingController {
  private final ChargingApplicationService service; public ChargingController(ChargingApplicationService service){this.service=service;}
  @PostMapping public ApiResponse<ChargingSessionView> start(@Valid @RequestBody StartChargingRequest r){return ApiResponse.success(service.start(r));}
  @PostMapping("/{sessionNo}/stop") public ApiResponse<ChargingSessionView> stop(@PathVariable String sessionNo,@Valid @RequestBody StopChargingRequest request){return ApiResponse.success(service.stop(sessionNo,request.requestId()));}
  @GetMapping("/{sessionNo}") public ApiResponse<ChargingSessionView> get(@PathVariable String sessionNo){return ApiResponse.success(service.view(sessionNo));}
}
