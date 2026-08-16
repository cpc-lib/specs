package com.example.evcharging.payment.integration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name="charging-core", path="/internal-api/v1/core/orders")
public interface CoreOrderClient {
  @GetMapping("/{orderNo}/payment-snapshot") OrderPaymentSnapshot paymentSnapshot(@PathVariable String orderNo);
}
