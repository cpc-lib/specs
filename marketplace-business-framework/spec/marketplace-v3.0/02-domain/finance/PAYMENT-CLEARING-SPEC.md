# Payment Clearing SPEC

## Boundary
PaymentTransaction proves provider-confirmed payment.
Finance converts it into operational clearing facts.

## PaymentClearingRecord
- clearingNo
- paymentTransactionId
- paymentNo
- tradeId
- channel
- providerMerchantRef
- currency
- grossReceivedAmount
- providerFeeEstimated/confirmed
- clearingAmount
- status

Status:
CREATED -> ALLOCATED -> RECONCILED -> CLOSED
CREATED/ALLOCATED -> EXCEPTION

## PaymentClearingAllocation
Allocation types:
- BUYER_CASH_TO_MERCHANT_PENDING
- SHIPPING_TO_MERCHANT_PENDING
- TAX_TO_SELLER_PENDING
- PLATFORM_SERVICE_AMOUNT
- PROVIDER_FEE
- OTHER_POLICY_BUCKET

## Invariants
- one successful PaymentTransaction has exactly one active PaymentClearingRecord;
- sum clearing allocations equals configured clearing base;
- allocation is append-only;
- PaymentSucceeded duplicate events create no duplicate clearing effect;
- merchant balance is changed only through merchant funds ledger posting command;
- provider fees are confirmed/reconciled later if callback does not include authoritative fee.
