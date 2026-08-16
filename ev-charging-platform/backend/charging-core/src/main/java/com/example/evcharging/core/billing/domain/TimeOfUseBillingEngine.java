package com.example.evcharging.core.billing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

/**
 * Deterministic time-of-use billing engine.
 *
 * Price-boundary meter values are interpolated from surrounding meter points.
 * The same boundary meter is reused by adjacent segments, therefore segment
 * energy always conserves the session's total meter delta.
 */
public final class TimeOfUseBillingEngine {

    public TimeOfUseBillingResult calculate(TimeOfUseBillingContext context) {
        validate(context);
        List<MeterPoint> points = normalizePoints(context);
        List<Instant> boundaries = boundaries(context);
        List<BillingSegment> segments = new ArrayList<>();
        long energyAmount = 0;
        long serviceAmount = 0;
        long previousMeter = context.startMeterWh();

        for (int i = 0; i < boundaries.size() - 1; i++) {
            Instant start = boundaries.get(i);
            Instant end = boundaries.get(i + 1);
            long startMeter = i == 0 ? context.startMeterWh() : previousMeter;
            long endMeter = i == boundaries.size() - 2
                    ? context.endMeterWh()
                    : interpolateMeter(end, points);
            if (endMeter < startMeter) throw new IllegalArgumentException("meter rollback across billing segment");
            long energy = endMeter - startMeter;
            PricingPeriod period = periodAt(start, context.stationZoneId(), context.periods());
            long energyFen = MvpBillingMath.feeFen(energy, period.energyPriceMicro());
            long serviceFen = MvpBillingMath.feeFen(energy, period.servicePriceMicro());
            segments.add(new BillingSegment(
                    i + 1, period.periodType(), start, end, startMeter, endMeter, energy,
                    period.energyPriceMicro(), period.servicePriceMicro(), energyFen, serviceFen));
            energyAmount = Math.addExact(energyAmount, energyFen);
            serviceAmount = Math.addExact(serviceAmount, serviceFen);
            previousMeter = endMeter;
        }

        long totalEnergy = context.endMeterWh() - context.startMeterWh();
        long segmentedEnergy = segments.stream().mapToLong(BillingSegment::energyWh).sum();
        if (segmentedEnergy != totalEnergy) throw new IllegalStateException("billing energy conservation violated");
        long receivable = Math.addExact(energyAmount, serviceAmount);
        return new TimeOfUseBillingResult(totalEnergy, energyAmount, serviceAmount, 0, 0, 0, receivable, List.copyOf(segments));
    }

    private static void validate(TimeOfUseBillingContext c) {
        Objects.requireNonNull(c.stationZoneId(), "stationZoneId");
        Objects.requireNonNull(c.chargingStartTime(), "chargingStartTime");
        Objects.requireNonNull(c.chargingEndTime(), "chargingEndTime");
        if (!c.chargingEndTime().isAfter(c.chargingStartTime())) throw new IllegalArgumentException("invalid charging interval");
        if (c.startMeterWh() < 0 || c.endMeterWh() < c.startMeterWh()) throw new IllegalArgumentException("invalid meter interval");
        if (c.periods() == null || c.periods().isEmpty()) throw new IllegalArgumentException("pricing periods are required");
        validateCoverage(c.periods());
    }

    private static void validateCoverage(List<PricingPeriod> periods) {
        List<PricingPeriod> sorted = periods.stream().sorted(Comparator.comparingInt(PricingPeriod::startMinute)).toList();
        int cursor = 0;
        for (PricingPeriod p : sorted) {
            if (p.startMinute() != cursor) throw new IllegalArgumentException("pricing periods contain gap/overlap at minute " + cursor);
            cursor = p.endMinute();
        }
        if (cursor != 1440) throw new IllegalArgumentException("pricing periods must cover 24 hours");
    }

    private static List<MeterPoint> normalizePoints(TimeOfUseBillingContext c) {
        ArrayList<MeterPoint> points = new ArrayList<>();
        points.add(new MeterPoint(c.chargingStartTime(), c.startMeterWh()));
        if (c.meterPoints() != null) {
            c.meterPoints().stream()
                    .filter(p -> !p.occurredAt().isBefore(c.chargingStartTime()) && !p.occurredAt().isAfter(c.chargingEndTime()))
                    .sorted(Comparator.comparing(MeterPoint::occurredAt))
                    .forEach(points::add);
        }
        points.add(new MeterPoint(c.chargingEndTime(), c.endMeterWh()));
        points.sort(Comparator.comparing(MeterPoint::occurredAt));
        long previous = -1;
        for (MeterPoint p : points) {
            if (previous >= 0 && p.meterWh() < previous) throw new IllegalArgumentException("meter rollback detected");
            previous = p.meterWh();
        }
        return points;
    }

    private static List<Instant> boundaries(TimeOfUseBillingContext c) {
        TreeSet<Instant> result = new TreeSet<>();
        result.add(c.chargingStartTime());
        result.add(c.chargingEndTime());
        ZonedDateTime localStart = c.chargingStartTime().atZone(c.stationZoneId());
        ZonedDateTime localEnd = c.chargingEndTime().atZone(c.stationZoneId());
        LocalDate day = localStart.toLocalDate().minusDays(1);
        LocalDate finalDay = localEnd.toLocalDate().plusDays(1);
        for (; !day.isAfter(finalDay); day = day.plusDays(1)) {
            for (PricingPeriod p : c.periods()) {
                if (p.startMinute() == 0) {
                    addBoundary(result, day.atStartOfDay(c.stationZoneId()).toInstant(), c);
                } else {
                    LocalTime time = LocalTime.of(p.startMinute() / 60, p.startMinute() % 60);
                    addBoundary(result, ZonedDateTime.of(day, time, c.stationZoneId()).toInstant(), c);
                }
            }
        }
        return List.copyOf(result);
    }

    private static void addBoundary(Set<Instant> set, Instant boundary, TimeOfUseBillingContext c) {
        if (boundary.isAfter(c.chargingStartTime()) && boundary.isBefore(c.chargingEndTime())) set.add(boundary);
    }

    private static PricingPeriod periodAt(Instant instant, ZoneId zone, List<PricingPeriod> periods) {
        ZonedDateTime zdt = instant.atZone(zone);
        int minute = zdt.getHour() * 60 + zdt.getMinute();
        return periods.stream().filter(p -> p.containsMinute(minute)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no pricing period for local minute " + minute));
    }

    private static long interpolateMeter(Instant target, List<MeterPoint> points) {
        MeterPoint left = points.getFirst();
        MeterPoint right = points.getLast();
        for (int i = 0; i < points.size() - 1; i++) {
            MeterPoint a = points.get(i);
            MeterPoint b = points.get(i + 1);
            if (!target.isBefore(a.occurredAt()) && !target.isAfter(b.occurredAt())) {
                left = a;
                right = b;
                break;
            }
        }
        if (target.equals(left.occurredAt())) return left.meterWh();
        if (target.equals(right.occurredAt())) return right.meterWh();
        long millis = Duration.between(left.occurredAt(), right.occurredAt()).toMillis();
        if (millis <= 0) return right.meterWh();
        long targetMillis = Duration.between(left.occurredAt(), target).toMillis();
        long delta = right.meterWh() - left.meterWh();
        long allocated = BigDecimal.valueOf(delta).multiply(BigDecimal.valueOf(targetMillis))
                .divide(BigDecimal.valueOf(millis), 0, RoundingMode.HALF_UP).longValueExact();
        return Math.addExact(left.meterWh(), allocated);
    }
}
