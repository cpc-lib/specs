package com.example.evcharging.finance.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="charging-core",path="/internal-api/v1/core/orders")
public interface CoreOrderProjectionClient {
    @GetMapping("/{orderNo}/payment-snapshot")
    Snapshot snapshot(@PathVariable String orderNo);

    record Snapshot(String orderNo,long userId,long stationId,long receivableAmountFen,long paidAmountFen,
                    String tradeStatus,String paymentStatus){}
}
