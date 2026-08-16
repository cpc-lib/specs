# Station DataScope Projection — SPEC 8.1

## Why projection is required

A microservice must not query another service database directly to authorize a Merchant query.

Therefore each bounded context stores the station dimension needed for its own merchant read model.

## Projection chain

```text
ChargeOrder.stationId
        ↓ internal payment snapshot
PaymentOrder.stationId
        ↓ PaymentSucceeded event
FinanceTransactionFact.stationId
        ↓ reconciliation
SettlementSource.stationId
```

Operation:

```text
Device Alarm
    ↓
Asset internal device context
    ↓
OperationAlarm.stationId
    ↓
WorkOrder.stationId
```

## Local authorization query

Station-scoped Merchant queries become:

```sql
WHERE tenant_id = ?
  AND station_id IN (?, ?, ...)
```

The application never:

1. fetches all tenant rows;
2. sends them to the browser;
3. expects the browser to filter.

## Historical backfill

Projection repair uses authoritative service APIs.

Financial amounts and immutable business facts are not changed.

`station_id` is treated as authorization/read-model metadata and may be backfilled even for already settled business.
