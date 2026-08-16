# Refund Reverse Clearing SPEC

## Goal
Refund reverses the original economics, not merely the channel payment.

## RefundReverseAllocation
For each refund line store:
- refundNo
- originalOrderItemId
- originalFundingAllocationId
- reverseType
- amount
- currency
- settlementImpact
- ledgerImpact

ReverseType:
- BUYER_CASH_REFUND
- PLATFORM_SUBSIDY_REVERSAL
- MERCHANT_ENTITLEMENT_REVERSAL
- SHOP_ENTITLEMENT_REVERSAL
- SHIPPING_REVERSAL
- TAX_REVERSAL
- COMMISSION_REVERSAL
- PAYMENT_FEE_ADJUSTMENT

## Before Merchant Settlement
Refund reduces pending/eligible merchant economic amount and posts reverse ledger facts.

## After Merchant Settlement/Payout
Closed settlement is immutable.
Create SettlementAdjustment and, if needed, Merchant NEGATIVE balance/future settlement deduction.

## Invariants
- sum buyer cash refund reverse allocations = successful provider refund amount;
- original funding allocation cannot be reversed beyond its refundable/reversible amount;
- refund UNKNOWN does not post final reverse clearing until provider is authoritative;
- adjustment commands are idempotent.
