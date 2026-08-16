# Maintenance Workflow

## Flowable role

Flowable orchestrates human tasks only:

1. Dispatch
2. Repair
3. Verify
4. Failed verification loops to Repair

Business truth remains in `operation_work_order`.

Do not treat Flowable variables as the authoritative work-order database.

## Maker-checker style verification

The assigned repair engineer cannot perform final verification.

## SLA

Every work order gets:

- response_due_time
- resolution_due_time

The SLA scanner persists unique breach facts:

- RESPONSE
- RESOLUTION

A scheduler retry does not create duplicate breach records.
