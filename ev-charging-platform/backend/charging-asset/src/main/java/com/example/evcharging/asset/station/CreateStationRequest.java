package com.example.evcharging.asset.station;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateStationRequest(
        @NotNull Long operatorId,
        @NotBlank @Size(max = 64)
        @Pattern(regexp = "[A-Za-z0-9_-]+", message = "must contain only letters, numbers, _ or -")
        String stationCode,
        @NotBlank @Size(max = 128) String stationName
) {}
