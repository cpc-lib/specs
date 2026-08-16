# Adjustment & Reversal

## Why append-only

Financial repair must not execute SQL like:

```sql
UPDATE finance_transaction_fact SET amount_fen = ...;
```

Instead, use `finance_adjustment_order`.

Supported 7.7 types:

- `PAYMENT_AMOUNT`
- `REFUND_AMOUNT`

`amount_fen` is signed.

Example:

```text
Original payment    10000
Adjustment             -1
Effective payment    9999
```

Reconciliation persists both the original and adjustment values for audit.

## Maker-checker

Creator and approver must be different users. A posted adjustment writes a balanced Ledger transaction.

## Reversal

A reversal creates another adjustment with the opposite signed amount and `reverses_adjustment_id`. Only one active reversal may exist for an adjustment.
