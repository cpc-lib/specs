package com.example.evcharging.core.billing.domain;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TimeOfUseBillingEngineTest {
    private final TimeOfUseBillingEngine engine = new TimeOfUseBillingEngine();
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Test
    void splitsEnergyAtPriceBoundaryAndConservesEnergy() {
        Instant start = ZonedDateTime.of(2026,8,10,7,30,0,0,ZONE).toInstant();
        Instant end = ZonedDateTime.of(2026,8,10,8,30,0,0,ZONE).toInstant();
        var periods = List.of(
                new PricingPeriod(1,"VALLEY",0,480,400000,200000),
                new PricingPeriod(2,"PEAK",480,1440,1000000,200000));
        var result = engine.calculate(new TimeOfUseBillingContext(ZONE,start,end,0,10000,List.of(),periods));
        assertEquals(10000,result.energyWh());
        assertEquals(2,result.segments().size());
        assertEquals(5000,result.segments().get(0).energyWh());
        assertEquals(5000,result.segments().get(1).energyWh());
        assertEquals(700,result.energyAmountFen());
        assertEquals(200,result.serviceAmountFen());
        assertEquals(900,result.receivableAmountFen());
    }

    @Test
    void handlesCrossMidnightDeterministically() {
        Instant start = ZonedDateTime.of(2026,8,10,23,30,0,0,ZONE).toInstant();
        Instant end = ZonedDateTime.of(2026,8,11,0,30,0,0,ZONE).toInstant();
        var periods = List.of(
                new PricingPeriod(1,"VALLEY",0,480,350000,100000),
                new PricingPeriod(2,"FLAT",480,1320,650000,100000),
                new PricingPeriod(3,"VALLEY",1320,1440,350000,100000));
        var result = engine.calculate(new TimeOfUseBillingContext(ZONE,start,end,1000,7000,List.of(),periods));
        assertEquals(6000,result.energyWh());
        assertEquals(2,result.segments().size());
        assertEquals(6000,result.segments().stream().mapToLong(BillingSegment::energyWh).sum());
        assertEquals(210,result.energyAmountFen());
        assertEquals(60,result.serviceAmountFen());
    }

    @Test
    void rejectsMeterRollback() {
        Instant start=Instant.parse("2026-08-10T00:00:00Z");
        Instant end=start.plusSeconds(3600);
        var periods=List.of(new PricingPeriod(1,"FLAT",0,1440,600000,300000));
        assertThrows(IllegalArgumentException.class,()->engine.calculate(new TimeOfUseBillingContext(ZoneId.of("UTC"),start,end,1000,900,List.of(),periods)));
    }
    @Test
    void usesObservedMeterPointsForBoundaryInterpolation() {
        Instant start = ZonedDateTime.of(2026,8,10,7,0,0,0,ZONE).toInstant();
        Instant boundaryPoint = ZonedDateTime.of(2026,8,10,7,45,0,0,ZONE).toInstant();
        Instant end = ZonedDateTime.of(2026,8,10,9,0,0,0,ZONE).toInstant();
        var periods = List.of(
                new PricingPeriod(1,"VALLEY",0,480,400000,0),
                new PricingPeriod(2,"PEAK",480,1440,1000000,0));
        // 0 -> 6kWh in first 45m, then 6kWh -> 10kWh in the next 75m.
        // Interpolated 08:00 meter is 6.8kWh, not the 5kWh produced by whole-session linear allocation.
        var result = engine.calculate(new TimeOfUseBillingContext(ZONE,start,end,0,10000,
                List.of(new MeterPoint(boundaryPoint,6000)),periods));
        assertEquals(6800,result.segments().get(0).energyWh());
        assertEquals(3200,result.segments().get(1).energyWh());
        assertEquals(592,result.energyAmountFen());
    }

    @Test
    void rejectsPriceScheduleWithGap() {
        Instant start=Instant.parse("2026-08-10T00:00:00Z");
        Instant end=start.plusSeconds(3600);
        var periods=List.of(
                new PricingPeriod(1,"VALLEY",0,400,400000,0),
                new PricingPeriod(2,"PEAK",480,1440,1000000,0));
        assertThrows(IllegalArgumentException.class,()->engine.calculate(new TimeOfUseBillingContext(ZoneId.of("UTC"),start,end,0,1000,List.of(),periods)));
    }
}
