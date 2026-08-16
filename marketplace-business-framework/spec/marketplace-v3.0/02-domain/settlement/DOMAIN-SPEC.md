# Settlement Domain SPEC
Aggregates:
- SettlementPolicy
- SettlementBatch
- SettlementItem
- MerchantPayable
- MerchantBalanceAccount
- PayoutOrder
- SettlementAdjustment

Eligibility:
received/completed + hold window + no blocking dispute/aftersale/risk freeze.

Immutable snapshot includes:
merchant gross
commission
payment fee
merchant discount
platform subsidy
refund adjustment
compensation
tax/fee
final payable

Closed settlement cannot be edited; use SettlementAdjustment.
Payout UNKNOWN requires provider query before retry.
