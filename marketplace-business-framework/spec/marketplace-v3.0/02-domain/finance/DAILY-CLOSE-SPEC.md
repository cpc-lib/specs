# Finance Daily Close SPEC

## FinanceDailyClose
States: CREATED -> RUNNING -> REVIEW_REQUIRED / PASSED -> CLOSED

Checks:
- successful payment without clearing
- clearing without trade allocation
- allocation sum mismatch
- merchant pending balance mismatch
- eligible record consumed twice
- approved settlement without payable
- payable without payout reservation consistency
- payout SUCCESS without ledger posting
- refund SUCCESS without reverse clearing
- merchant negative balance mismatch
- unbalanced ledger entry
- unreconciled CRITICAL provider statement

Close is a control fact, not permission to delete/modify historical rows.
