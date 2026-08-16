# SPEC 7.7 — Finance Hardening

## Scope

This release hardens the Finance vertical slice around six controls:

1. T+1 automatic reconciliation.
2. Raw channel-bill archive before normalization.
3. Append-only Adjustment / Reversal.
4. Settlement maker-checker approval.
5. Settlement ledger posting.
6. Invoice / red-flush provider abstraction.

## Non-negotiable financial invariants

- Original Payment / Refund facts are never overwritten by an adjustment.
- `1 fen` difference is never silently tolerated.
- Only `MATCH` may generate a Settlement Source.
- Settlement calculation does not mean settlement completion.
- Settlement completion requires approval by a different user when a human maker exists.
- `SUM(SettlementDetail.amount_fen) == SettlementBaseAmount`.
- Settlement approval posts a balanced ledger transaction before a source becomes `SETTLED`.
- Invoice provider calls execute outside the database transaction.
- Provider retries use the same business request number for idempotency.

## Settlement lifecycle

```text
READY Settlement Source
      ↓
CALCULATE
      ↓
ALLOCATED
      ↓
PENDING_APPROVAL
      ├─ REJECT → READY
      └─ APPROVE
             ↓
       Ledger Posting
             ↓
          SETTLED
```

## Adjustment lifecycle

```text
Original immutable fact
      ↓
Adjustment(PENDING_APPROVAL)
      ↓ maker-checker
POSTED adjustment fact
      ↓
Reconciliation uses original + posted adjustments
```

A reversal is another signed adjustment linked to the original adjustment. The original adjustment is not deleted or mutated.
