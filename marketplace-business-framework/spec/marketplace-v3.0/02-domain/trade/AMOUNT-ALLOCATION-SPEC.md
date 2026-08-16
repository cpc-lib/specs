# Trade Amount / Discount Allocation SPEC

## 1. Amount Fields
Trade:
- goodsAmount
- shippingAmount
- taxAmount
- discountAmount
- payableAmount

Invariant:
`payableAmount = goodsAmount + shippingAmount + taxAmount - discountAmount`

## 2. DiscountAllocation
Every discount is decomposed to OrderItem and funding party.
Fields:
- tradeId
- merchantOrderId
- orderItemId
- discountSourceType
- discountSourceId
- fundingPartyType
- fundingMerchantId nullable
- originalDiscountAmount
- allocatedDiscountAmount
- roundingResidual
- calculationTrace

FundingPartyType:
PLATFORM / MERCHANT / SHOP / BRAND / OTHER_APPROVED

## 3. Conservation
For each discount source:
`sum(allocatedDiscountAmount) = sourceDiscountAmount`

For Trade:
`sum(item finalAmount) + explicitly allocated shipping/tax = payableAmount`

## 4. RoundingResidualPolicy
Default:
1. calculate at high precision;
2. round each line to currency scale;
3. compute residual;
4. assign residual to highest eligible base amount; tie-break by smallest stable OrderItem ID;
5. store residual explicitly.

No hidden 0.01 loss is allowed.

## 5. Refund
Refund calculator must use original allocation snapshot. It must not redistribute the original order discount based on remaining items after return.
