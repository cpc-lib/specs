package com.example.evcharging.asset.charger;
import jakarta.validation.constraints.NotBlank;
public record CreateChargerRequest(@NotBlank String chargerCode, @NotBlank String deviceSn, String protocolType) {}
