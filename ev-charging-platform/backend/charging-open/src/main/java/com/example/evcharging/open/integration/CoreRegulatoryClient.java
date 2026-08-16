package com.example.evcharging.open.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="charging-core",path="/internal-api/v1/core/regulatory")
public interface CoreRegulatoryClient {
    @GetMapping("/orders/latest")
    List<OrderView> latest(@RequestParam int limit);

    record OrderView(String orderNo,String sessionNo,long stationId,long energyWh,long energyAmountFen,
                     long serviceAmountFen,long receivableAmountFen,long paidAmountFen,
                     int tradeStatus,int paymentStatus,String finishTime,String updateTime){}
}
