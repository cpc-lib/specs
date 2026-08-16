# Regulatory Integration Architecture

## Adapter boundary

```text
Domain / Read Model
      ↓
Regulatory Snapshot Scheduler
      ↓
Regulatory Report Task
      ↓
RegulatoryProtocolAdapter
      ↓
Platform-specific payload/security
      ↓
Regulatory endpoint
```

Current adapter:

`GB_T_44130_2025_CANONICAL`

Future examples:

- `PROVINCE_A_V2026`
- `CITY_B_GBT44130_PROFILE`
- `GRID_OPERATOR_C`

A local regulatory protocol must be implemented as another adapter rather than branching inside Charging, Payment or Finance.

## Report categories

SPEC 8.2:

- `PUBLIC_STATION`
- `BUSINESS_ORDER`

The scheduler snapshots authoritative local read models. Payload hash uniqueness prevents resending an unchanged snapshot as a new logical report task.

## Retry model

`PENDING → SENDING → SENT`

Failures:

`SENDING → RETRY → ... → DEAD`

Each dispatch is claim-token protected.

A crashed worker leaves a stale SENDING claim; recovery returns it to RETRY after five minutes.

## Financial integrity

A regulatory HTTP failure never changes:

- charging session facts
- charge order
- payment facts
- ledger
- reconciliation
- settlement

Regulatory delivery is an external reporting concern.
