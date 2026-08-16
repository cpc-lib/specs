# Marketplace Finance Domain SPEC
Models:
- MarketplaceLedgerEntry
- MarketplaceLedgerLine
- ClearingAllocation
- AccountingPeriod

Accounts conceptually include:
PAYMENT_CLEARING
MERCHANT_PENDING
MERCHANT_PAYABLE
PLATFORM_COMMISSION
PROMOTION_EXPENSE
REFUND_PAYABLE
BANK

Double-entry invariant:
sum(debit) == sum(credit)
for every ledger entry.
