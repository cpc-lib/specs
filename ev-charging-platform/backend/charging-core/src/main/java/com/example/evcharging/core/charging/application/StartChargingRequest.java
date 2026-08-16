package com.example.evcharging.core.charging.application;
import jakarta.validation.constraints.NotBlank;
public record StartChargingRequest(@NotBlank String requestId,@NotBlank String connectorCode,Long vehicleId) {}
