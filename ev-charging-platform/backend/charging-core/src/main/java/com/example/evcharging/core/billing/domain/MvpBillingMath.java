package com.example.evcharging.core.billing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MvpBillingMath {
    private MvpBillingMath() {}
    public static long feeFen(long energyWh,long priceMicroPerKwh){
        if(energyWh<0 || priceMicroPerKwh<0) throw new IllegalArgumentException("negative billing input");
        return BigDecimal.valueOf(energyWh).multiply(BigDecimal.valueOf(priceMicroPerKwh))
                .divide(BigDecimal.valueOf(10_000_000L),0,RoundingMode.HALF_UP).longValueExact();
    }
}
