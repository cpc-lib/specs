package com.example.evcharging.payment.application;
import jakarta.validation.constraints.NotBlank;
public record CreatePaymentRequest(@NotBlank String requestId,@NotBlank String orderNo,@NotBlank String channel) {}
