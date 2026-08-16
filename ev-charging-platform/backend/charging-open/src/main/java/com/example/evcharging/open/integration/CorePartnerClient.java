package com.example.evcharging.open.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="charging-core",path="/internal-api/v1/core/partner")
public interface CorePartnerClient {
    @PostMapping("/charging/start")
    SessionView start(@RequestBody StartCommand command);

    @GetMapping("/charging/{sessionNo}")
    SessionView session(@PathVariable String sessionNo,@RequestParam long localUserId);

    @PostMapping("/charging/{sessionNo}/stop")
    SessionView stop(@PathVariable String sessionNo,@RequestBody StopCommand command);

    @GetMapping("/orders/{orderNo}")
    OrderSnapshot order(@PathVariable String orderNo);

    record StartCommand(long localUserId,String requestId,String connectorCode){}
    record StopCommand(long localUserId,String requestId){}
    record SessionView(String sessionNo,String status,long connectorId,long energyWh,Integer soc,Long powerW,
                       Long receivableAmountFen,String orderNo){}
    record OrderSnapshot(String orderNo,String sessionNo,long localUserId,long stationId,long energyWh,
                         long receivableAmountFen,long paidAmountFen,int tradeStatus,int paymentStatus,String finishTime){}
}
