# SPEC 7.6 — Settlement Vertical Slice

Only `finance_settlement_source.status=READY` is eligible. Rules are versioned. 7.6 implements deterministic `RATIO_BPS` allocation. For every order: `SUM(finance_settlement_detail.amount_fen) == settlement_base_amount_fen`. Rounding residual is deterministically assigned to PLATFORM. Unbalanced settlement fails before the source is marked SETTLED.
