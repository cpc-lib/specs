# Alarm Specification

## Device event contract

Raised:

```text
ALARM|alarmCode|severity|connectorNo|metricValue|metricUnit|message|eventTimeMs
```

Recovered:

```text
ALARM_RECOVERED|alarmCode|connectorNo|eventTimeMs
```

The IoT Gateway converts these authenticated device messages into `ev.device.alarm.v1`.

## Severity

- INFO
- WARNING
- MAJOR
- CRITICAL

Default policy when no tenant rule exists:

- INFO/WARNING: record only
- MAJOR/CRITICAL: create work order

Tenant rules can raise the minimum severity or disable auto work-order creation.

## Deduplication

`operation_active_alarm` is the database-level active-fault guard.

This is intentionally separate from historical `operation_alarm`, because MySQL has no simple portable partial unique index for "unique only while ACTIVE".
