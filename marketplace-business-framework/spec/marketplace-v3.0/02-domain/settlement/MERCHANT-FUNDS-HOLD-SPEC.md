# Merchant Funds Hold SPEC

Aggregate: MerchantFundsHold

Fields:
holdNo
merchantId
scopeType
scopeId
currency
holdAmount(optional)
reasonType
sourceType/sourceId
status
effectiveAt
expireAt
reviewRequired
releasedBy
releaseReason
version

Scope:
ALL_FUNDS
SETTLEMENT_BATCH
MERCHANT_PAYABLE
PAYOUT
MERCHANT_ORDER
ORDER_ITEM
AMOUNT

States:
CREATED -> ACTIVE -> RELEASE_PENDING -> RELEASED
ACTIVE -> EXPIRED / CANCELLED only by policy.

A hold must affect settlement/payout through explicit checks and balance-ledger/eligibility operations.
