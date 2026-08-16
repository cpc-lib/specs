# SPEC 7.6 — Reconciliation Vertical Slice

Finance does not JOIN the Payment service database. `PaymentSucceeded/RefundSucceeded` are projected into Finance facts. Matching is exact by `paymentNo`, then `channelTradeNo`. Results: MATCH, LOCAL_ONLY, CHANNEL_ONLY, AMOUNT_MISMATCH, REFUND_MISMATCH, STATUS_MISMATCH. **100.00 vs 99.99 is always a mismatch.** Only MATCH can create `finance_settlement_source`. Resolving a Difference Case records investigation only; it never rewrites original facts or silently enables settlement.
