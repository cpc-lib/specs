# Financial Reconciliation SPEC V2.2

## Layers
1. PSP Payment Statement ↔ PaymentTransaction
2. PaymentTransaction ↔ PaymentClearingRecord
3. PaymentClearing ↔ TradePaymentAllocation / FundingAllocation
4. Merchant Pending Ledger ↔ SettlementEligibility
5. SettlementEligibility ↔ SettlementItem
6. SettlementBatch ↔ MerchantPayable
7. PayoutOrder/Transaction ↔ PSP/Bank Payout Statement
8. All financial facts ↔ MarketplaceLedger

## Results
MATCHED
LOCAL_MISSING
PROVIDER_MISSING
DUPLICATE
AMOUNT_MISMATCH
CURRENCY_MISMATCH
STATUS_MISMATCH
CLEARING_MISMATCH
FUNDING_MISMATCH
MERCHANT_BALANCE_MISMATCH
SETTLEMENT_MISMATCH
PAYOUT_MISMATCH
LEDGER_MISMATCH

## Rule
Any unexplained ¥0.01 is mismatch.
Known provider fee/FX must have explicit model and source, never implicit tolerance.
