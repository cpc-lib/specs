# SPEC 7.3 Release Notes — Vertical Slice + One Person / AI Plan

## Scope

SPEC 7.3 extends 7.2 with the first executable business vertical slice:

```text
Station
  -> Charger
  -> Connector
  -> Device Simulator Online
  -> Start Charging
  -> Device START_CHARGING
  -> CHARGING_STARTED
  -> Telemetry
  -> Stop Charging
  -> CHARGING_STOPPED
  -> Billing
  -> ChargeOrder
```

## Added

- Charger CRUD and Connector CRUD in Asset service.
- Internal Connector Snapshot API for service-to-service charging validation.
- Charging session persistence schema.
- Connector active-session uniqueness table.
- Billing snapshot persistence.
- Meter records and ChargeOrder schema.
- Device-command Outbox for Start/Stop commands.
- Core Start/Stop/Query charging APIs.
- ChargingSession stores device routing snapshot (`device_id`, `connector_no`) so Stop does not join Asset-owned tables.
- Asset OpenFeign client from Core.
- Core device-online Redis validation.
- IoT normalized charging-event publication to Kafka.
- Simulator START/STOP behavior with generated telemetry.
- Core Kafka consumer for charging started / telemetry / stopped.
- Simple deterministic MVP billing calculation using immutable snapshot prices.
- One-person + AI 47-week production-v1 schedule, with intermediate MVP milestones.

## Status

This package remains a release candidate. Static checks and pure-JDK checks are executable in the current environment; full Maven/Docker/Testcontainers/npm release gates still require an environment with dependency and Docker runtime access.
