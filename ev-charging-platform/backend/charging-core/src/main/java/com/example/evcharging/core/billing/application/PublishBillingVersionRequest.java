package com.example.evcharging.core.billing.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record PublishBillingVersionRequest(
        @NotBlank String templateName,
        @NotBlank String versionNo,
        @NotBlank String timezone,
        @NotNull LocalDateTime effectiveFrom,
        @NotEmpty List<@Valid PeriodRequest> periods
) {
    public record PeriodRequest(
            @NotBlank String periodType,
            @Min(0) @Max(1439) int startMinute,
            @Min(1) @Max(1440) int endMinute,
            @PositiveOrZero long energyPriceMicro,
            @PositiveOrZero long servicePriceMicro
    ) {}
}
