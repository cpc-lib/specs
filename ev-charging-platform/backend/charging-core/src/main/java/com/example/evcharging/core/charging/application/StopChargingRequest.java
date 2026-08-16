package com.example.evcharging.core.charging.application;
import jakarta.validation.constraints.NotBlank;
public record StopChargingRequest(@NotBlank String requestId) {}
