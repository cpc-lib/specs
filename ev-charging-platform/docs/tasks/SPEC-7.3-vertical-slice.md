# SPEC 7.3 Vertical Slice Tasks

## Target

```text
Station -> Charger -> Connector -> Online Device -> Start -> Telemetry -> Stop -> Billing -> Order
```

## Tasks

### AST-009 Charger CRUD
- Create/list Charger under Station.
- Tenant isolation.
- Unique tenant + chargerCode.

### AST-010 Connector CRUD
- Create/list Connector under Charger.
- Unique charger + connectorNo.
- Internal ConnectorSnapshot endpoint.

### CHG-013 Start Charging Persistence
- Validate ConnectorSnapshot.
- Validate Redis device online key.
- Insert ChargingSession.
- Insert connector_active_session.
- Freeze BillingSnapshot.
- Insert device_command_outbox in same local transaction.

### IOT-011 Charging Event Publisher
- Publish versioned DomainEventEnvelope for CHARGING_STARTED / TELEMETRY / CHARGING_STOPPED to Kafka.
- Kafka key = sessionNo.
- Core persists eventId in charging_device_event_inbox for at-least-once idempotency.

### SIM-003 Stateful Simulator
- START_CHARGING starts telemetry.
- STOP_CHARGING stops telemetry and emits final meter.

### CHG-014 Device Event Consumer
- STARTED -> CHARGING.
- TELEMETRY -> latest Redis + meter record.
- STOPPED -> billing + ChargeOrder + FINISHED + release ActiveSession.

### E2E-013 Vertical Slice
- Start simulator.
- Create Station/Charger/Connector.
- Verify device online.
- Start charging.
- Observe telemetry.
- Stop charging.
- Verify order amount and energy.
