# Merchant Exit / Shop Closure SPEC

Aggregate: MerchantExitCase

States:
REQUESTED
-> BUSINESS_FREEZE
-> PRODUCT_OFFBOARD
-> OPEN_ORDER_SETTLEMENT
-> AFTERSALE_WINDOW
-> FINANCIAL_RECONCILIATION
-> DEPOSIT_SETTLEMENT
-> DATA_RETENTION
-> CLOSED

Blockers:
open MerchantOrders
unfulfilled packages
active AfterSale/Dispute
Payment/Refund/Payout UNKNOWN
unconsumed SettlementEligibility
open MerchantPayable
negative merchant balance
critical reconciliation exception
active funds/legal/governance hold

Exit actions are idempotent and observable.
Shop closure may be narrower but must preserve merchant-level financial obligations.
