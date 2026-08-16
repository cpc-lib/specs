# Release Notes — SPEC 7.7 Finance Hardening RC

Status: `foundation-rc-finance-hardening`

## Added

- T+1 reconciliation scheduler and per-tenant schedule configuration.
- raw channel bill archive metadata and local development archive adapter.
- signed append-only adjustments and reversals.
- maker-checker user context.
- settlement calculation / approval separation.
- settlement ledger posting.
- invoice provider abstraction, mock issue and red-flush flow.
- Admin Adjustment / Invoice / settlement approval flows.

## Important accounting behavior

- historical payment/refund facts remain immutable.
- one-cent differences remain differences.
- settlement cannot complete before approval and balanced ledger posting.
- provider calls are outside DB transactions.

## Runtime gate

Still RC until Maven, MySQL/Flyway, Kafka, Docker/Testcontainers and frontend dependency builds run in a runtime-capable environment.
