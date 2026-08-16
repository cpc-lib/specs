package com.example.evcharging.core.billing.application;

import com.example.evcharging.core.billing.domain.PricingPeriod;
import java.util.List;

public record BillingSnapshotDocument(
        String versionNo,
        long templateId,
        long versionId,
        String timezone,
        List<PricingPeriod> periods
) {}
