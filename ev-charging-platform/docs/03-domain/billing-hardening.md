# Billing Hardening - SPEC 7.4

## Goals

- Published price versions are immutable business facts.
- Start charging freezes a billing snapshot.
- Charging spanning multiple price periods is segmented deterministically.
- Segment energy conserves the physical meter delta.
- Historical replay uses the original snapshot, not today's price.

## Price unit

`priceMicroPerKwh = CNY/kWh × 1,000,000`

Examples:

- ¥0.35/kWh = `350000`
- ¥0.65/kWh = `650000`
- ¥1.05/kWh = `1050000`

## Segmenting

The engine introduces boundaries at each local price-period start. Meter values at a
boundary are interpolated from surrounding meter points. The resulting boundary meter is
used by both adjacent segments, so:

`SUM(segment.energyWh) == finalMeterWh - initialMeterWh`

## MVP limitations

Parking fees, occupation fees, coupons and member discounts remain zero in SPEC 7.4.
Their fields are preserved so later phases can add them without changing the order contract.
