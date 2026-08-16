# Settlement Approval & Ledger Posting

## Calculation is not payment/settlement completion

`SettlementApplicationService.run` only:

- selects READY sources (max 500 per batch),
- calculates allocation,
- stores Settlement Order / Detail,
- claims source as `ALLOCATED`,
- produces `PENDING_APPROVAL` batch.

It does **not** mark the source `SETTLED`.

## Approval

Approval requires another user when `created_by > 0`.

For each settlement order:

```text
DEBIT  CHARGING_RECEIVABLE_CLEARING
CREDIT PLATFORM_REVENUE / SETTLEMENT_PAYABLE_*
```

The LedgerPosting constructor enforces total debit equals total credit.

Only after ledger posting succeeds does the source transition:

`ALLOCATED → SETTLED`.

## Rejection

Rejected batches return sources:

`ALLOCATED → READY`.

Rejected historical settlement orders stay immutable as audit records; the source can participate in a new settlement attempt.
