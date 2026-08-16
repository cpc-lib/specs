package com.example.evcharging.core.charging.realtime;

import com.example.evcharging.framework.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/v1/charging/sessions")
public class ChargingRealtimeTicketController {
    private final ChargingRealtimeTicketService service;
    public ChargingRealtimeTicketController(ChargingRealtimeTicketService service){this.service=service;}

    @PostMapping("/{sessionNo}/realtime-ticket")
    public ApiResponse<ChargingRealtimeTicketService.Ticket> ticket(@PathVariable String sessionNo){
        return ApiResponse.success(service.issue(sessionNo));
    }
}
