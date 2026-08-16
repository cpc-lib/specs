# Invoice / Red Flush — SPEC 7.7

## Current provider model

`InvoiceProvider` isolates third-party invoice vendors.

7.7 ships `MockInvoiceProvider` for deterministic development testing. A real provider adapter (for example NuoNuo or another compliant e-invoice provider) remains an external integration task.

## Transaction boundary

```text
reserve request (local DB tx)
      ↓ commit
provider API call (outside DB tx)
      ↓
confirm success/failure (local DB tx)
```

This prevents a third-party HTTP timeout from holding a database transaction open.

## Idempotency

- Issue uses stable `requestNo`.
- Red flush uses stable `redNo`.
- Failed attempts with the same requestId may retry.
- Successful retries return the original InvoiceNo / RedNo.

## Amount

The 7.7 MVP invoice amount is the net effective payment amount:

`Payment + posted payment adjustments - successful refunds - posted refund adjustments`.

Production tax-item breakdown must later be projected from the trade/billing domain rather than trusted from a client request.
