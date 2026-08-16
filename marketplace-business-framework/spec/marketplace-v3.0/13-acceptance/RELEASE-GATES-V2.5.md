# V2.5 Release Gates

Mandatory:
- merchant application cannot activate without required current verification/risk/qualification
- C2C identity and payout profile are independently verified
- shop/category saleability checks merchant/category admission, not Shop status only
- merchant deposit ledger is append-only and cannot go negative
- penalty execution is idempotent
- appeal reversal never deletes original violation/penalty facts
- prohibited/restricted goods policy is versioned and enforceable at publish/checkout as configured
- risk returns decision only; business contexts own mutations
- risk feature snapshots contain no raw credentials/secrets
- funds hold is an explicit scoped record
- active funds hold prevents affected settlement/payout/withdrawal
- merchant suspension still allows required refund/aftersale/provider callbacks
- merchant exit cannot close with configured blockers
- high-risk platform actions are audited with reason/policy/evidence/operator
