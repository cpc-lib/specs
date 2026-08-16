# Merchant Deposit SPEC

## Account
MerchantDepositAccount is liability/security balance.

## Transaction
Append-only MerchantDepositTransaction:
REQUIREMENT
RECEIPT
FREEZE
UNFREEZE
DEDUCTION
REFUND_RESERVATION
REFUND_SUCCESS
REFUND_RELEASE
ADJUSTMENT
REVERSAL

Invariant:
available = received - frozen - deducted - refunded - refundReserved >= 0.

Deposit deduction requires source:
ViolationCase / PenaltyAction / approved SettlementAdjustment.

Merchant suspension/exit cannot arbitrarily confiscate deposit.
Refund UNKNOWN keeps refund reservation occupied.
