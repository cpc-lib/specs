# Accounting Posting Matrix

This is a logical/default posting matrix. Statutory accounting policy is versioned and deployment/jurisdiction specific.
Every entry must balance and reference source business fact.

| Business Fact | Debit | Credit | Notes |
|---|---|---|---|
| Buyer provider payment confirmed | PSP_CLEARING_RECEIVABLE | TRADE_CLEARING_LIABILITY | before settlement eligibility |
| Platform-funded subsidy becomes owed to merchant | PLATFORM_PROMOTION_EXPENSE / SUBSIDY_CLEARING | MERCHANT_PENDING_LIABILITY | according to campaign policy |
| Merchant economic entitlement recognized to pending | TRADE_CLEARING_LIABILITY | MERCHANT_PENDING_LIABILITY | operational timing policy |
| Platform commission earned | MERCHANT_PENDING_LIABILITY | PLATFORM_COMMISSION_REVENUE | only when earned per policy |
| Provider fee confirmed | PAYMENT_FEE_EXPENSE or merchant charge | PSP_CLEARING_RECEIVABLE / MERCHANT_PENDING | policy-driven |
| Merchant settlement approved | MERCHANT_PENDING_LIABILITY | MERCHANT_PAYABLE | settlement batch |
| Payout succeeds | MERCHANT_PAYABLE | PSP/BANK_CLEARING | payout fact |
| Buyer refund succeeds | REFUND_CLEARING / relevant liability | PSP_CLEARING_RECEIVABLE/BANK | with reverse allocations |
| Refund after payout merchant responsibility | MERCHANT_NEGATIVE_RECEIVABLE / future deduction | REFUND_CLEARING | policy-driven |
| Settlement adjustment | source-specific | source-specific | append-only adjustment |

Hard invariant:
`sum(debit) = sum(credit)` per MarketplaceLedgerEntry.
