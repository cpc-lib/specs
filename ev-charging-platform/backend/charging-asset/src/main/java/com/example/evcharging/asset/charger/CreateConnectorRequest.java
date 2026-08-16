package com.example.evcharging.asset.charger;
import jakarta.validation.constraints.Min; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
public record CreateConnectorRequest(@NotBlank String connectorCode, @NotNull @Min(1) Integer connectorNo,
                                     @NotNull Integer connectorType, @Min(1) Long ratedPowerW) {}
