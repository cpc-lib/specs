# Merchant Funds Ledger SPEC

Merchant balance is a read/account view backed by append-only `merchant_balance_ledger`.

Balance buckets:
- PENDING
- AVAILABLE
- FROZEN
- SETTLING
- PAID
- NEGATIVE

Typical facts:
- PAYMENT_ALLOCATED -> PENDING
- SETTLEMENT_ELIGIBLE -> available-for-settlement fact
- SETTLEMENT_LOCK -> SETTLING
- PAYOUT_SUCCESS -> PAID/outflow
- REFUND_AFTER_SETTLEMENT -> NEGATIVE or future deduction
- RISK_FREEZE -> FROZEN

Never repair MerchantBalanceAccount by direct SQL. Use ledger adjustment/reversal command and reconcile balances.
