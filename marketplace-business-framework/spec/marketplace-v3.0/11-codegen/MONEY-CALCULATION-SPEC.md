# Money Calculation SPEC

## Currency
Currency scale defaults to ISO/business configuration. CNY uses scale=2.
All internal money calculations use BigDecimal and explicit RoundingMode.

## Trade
`tradePayable = goods + shipping + tax - discount`

## Item Discount Allocation
Weight-based default:
`raw_i = sourceDiscount * eligibleBase_i / sum(eligibleBase)`
Round to currency scale, then allocate residual deterministically.

## Refund
`maxRefundableItem = paidAllocatedItemAmount - successfulItemRefund - reservedItemRefund`
Shipping refund follows explicit AfterSalePolicy snapshot.

## Settlement
`merchantPayable = merchantGross - commission - merchantFees - merchantFundedDiscount - refundAdjustments - compensationDeductions + platformSubsidy + approvedAdjustments`

Every component must be persisted/snapshotted. No recomputation from current promotion/commission rules for historical transactions.
