# Reconciliation Domain SPEC
Layers:
1 payment statement ↔ PaymentTransaction
2 PaymentTransaction ↔ Trade allocation
3 refund ↔ channel refund
4 Trade ↔ settlement
5 payout ↔ bank/provider
6 settlement ↔ ledger

Results:
MATCHED
LOCAL_MISSING
CHANNEL_MISSING
AMOUNT_MISMATCH
STATUS_MISMATCH
REFUND_MISMATCH
ALLOCATION_MISMATCH
SETTLEMENT_MISMATCH
PAYOUT_MISMATCH

0.01 monetary difference is mismatch unless an explicit fee/FX rule explains it.
