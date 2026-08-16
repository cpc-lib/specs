package com.example.evcharging.payment.application;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
public record CreateRefundRequest(@NotBlank String requestId,@NotBlank String paymentNo,@Min(1) long amountFen,@NotBlank String reason) {}
