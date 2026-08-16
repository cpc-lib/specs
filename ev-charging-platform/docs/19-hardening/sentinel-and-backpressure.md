# Sentinel and Backpressure

## Gateway

Gateway rules protect route IDs.

Initial route groups:

- system
- asset
- core
- payment
- finance
- operation
- open
- iot

Sentinel's Gateway adapter supports route-level and API-group flow control.

## Business hot paths

Explicit resources:

- `charging.start`
- `charging.stop`
- `payment.create`
- `payment.refund`

Initial protection includes QPS rules.

`charging.start` and `payment.create` also have an exception-ratio circuit-breaker baseline.

## Dynamic rule migration

SPEC 8.3 RC includes safe code defaults so a service is not completely unprotected when the control plane is unavailable.

Production should move changeable rules to a persistent configuration source and keep versioned rule changes under release/audit control.

A dashboard-only pushed rule is not sufficient as the sole source of production policy.

## Backpressure

Persisted work queues:

- Outbox
- Partner Callback Task
- Regulatory Report Task

should reject scanner submission when the local executor is full.

They must not:

- create unlimited Java threads
- use an unbounded in-memory queue
- mark database tasks SENDING before a worker has capacity to execute them
