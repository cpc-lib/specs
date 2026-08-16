# Merchant Funds & Settlement Deepening SPEC

## Buckets
PENDING / AVAILABLE / FROZEN / SETTLING / PAID / NEGATIVE

## MerchantPayable
Created from APPROVED SettlementBatch.
Status:
OPEN -> PARTIALLY_RESERVED -> RESERVED -> PAYING -> PAID -> CLOSED
OPEN/RESERVED -> FROZEN
PAYING -> UNKNOWN / FAILED / PAID

## Settlement Hold
Hold sources:
- C2C_STANDARD_HOLD
- RISK
- DISPUTE
- AFTERSALE
- COMPLIANCE
- MANUAL_APPROVED

A hold has effective period, reason, source id, amount or scope, release condition and audit.

## Payout Reservation
Before calling PSP/bank:
lock MerchantPayable -> reserve amount -> create PayoutOrder -> commit -> call provider outside DB TX.
UNKNOWN retains reservation.

## Withdrawal
Optional `merchant_withdrawal_request` is enabled only when a configured licensed PSP/bank account product supports merchant-initiated withdrawal.
Withdrawal uses same payable/available-funds reservation rules as payout and cannot bypass settlement eligibility.

## Negative Balance
Refund/chargeback/compensation after payout may create NEGATIVE merchant balance.
Future eligible settlement first offsets NEGATIVE before creating new payable.
