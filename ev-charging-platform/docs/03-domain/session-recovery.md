# Charging Session Recovery - SPEC 7.4

## Trigger

A scheduled recovery scanner checks stale:

- STARTING
- STOPPING
- RECOVERING

## Recovery action

1. Lock one session in its own transaction.
2. Increment recovery attempt.
3. Move to `RECOVERING`.
4. Enqueue `QUERY_TRANSACTION` in the device command outbox.
5. Simulator/device replies with its transaction fact:
   - CHARGING_STARTED + original start meter, or
   - CHARGING_STOPPED + final meter.
6. Normal event consumer reconciles cloud state.

After `max-attempts`, the session moves to `MANUAL_REVIEW`.

## Important invariant

A recovery `CHARGING_STARTED` event never overwrites an existing initial meter.

## Event-time rule

Charging event timestamps are device facts. SPEC 7.4 stores charging/meter event time using a UTC convention and keeps operational `update_time` separate. Out-of-order or meter-rollback telemetry is stored with an invalid validation status and is excluded from financial billing.
