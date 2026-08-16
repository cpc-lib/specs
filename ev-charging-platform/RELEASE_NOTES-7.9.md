# Release Notes — SPEC 7.9

Status: `foundation-rc-operation-hardening`

## Added

- heartbeat deadline registry
- lease-safe offline detector
- deterministic device lifecycle events
- ONLINE/OFFLINE → DEVICE_OFFLINE alarm bridge
- notification policy/task/retry worker
- inspection plan/task scheduler
- inspection overdue facts
- spare-part catalog/stock/transactions
- requestId-idempotent stock movement
- no-negative-stock SQL guard
- work-order attachment abstraction
- local development attachment storage
- Technician UniApp
- Admin inspection/spare/notification pages

## Safety / correctness

- old connection timeout cannot delete a newer lease
- DEVICE_OFFLINE defaults to alarm-only; tenant rule required for auto work order
- notification calls run outside the alarm transaction
- technician attachment upload requires work-order assignment
- inspection generation is idempotent by plan/date
- stock consume uses atomic quantity guard

## Schedule change

Production V1 baseline changes from 47 weeks / 235 person-days to:

**50 weeks / 250 person-days**

The original four-week external integration buffer remains intact.

## Runtime gate

This remains RC until live Redis/Kafka/MySQL/Flowable/frontend/Technician App gates pass.
