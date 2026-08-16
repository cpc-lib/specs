# Funding Responsibility SPEC

## Purpose
Freeze the economic responsibility of buyer cash, platform subsidy and merchant/shop-funded discount at Trade creation.

## OrderItemEconomicsSnapshot
Fields:
- tradeId / merchantOrderId / orderItemId
- currency
- merchandiseGross
- allocatedShipping
- allocatedTax
- platformFundedDiscount
- merchantFundedDiscount
- shopFundedDiscount
- brandFundedDiscount
- otherFundedDiscount
- buyerCashAllocation
- merchantGrossEntitlement
- commissionBase
- settlementBase
- calculationTraceJson

## FundingAllocation
FundingSourceType:
- BUYER_CASH
- PLATFORM_COUPON
- PLATFORM_PROMOTION
- PLATFORM_POINTS_SUBSIDY
- MERCHANT_COUPON
- MERCHANT_PROMOTION
- SHOP_COUPON
- SHOP_PROMOTION
- BRAND_SUBSIDY
- OTHER_APPROVED

FundingPartyType:
- BUYER
- PLATFORM
- MERCHANT
- SHOP
- BRAND
- EXTERNAL_PARTNER

SettlementImpact:
- PRESERVE_MERCHANT_ENTITLEMENT
- REDUCE_MERCHANT_ENTITLEMENT
- INCREASE_MERCHANT_ENTITLEMENT
- NEUTRAL

## Core Equations
For one OrderItem:
`buyerPayable = merchandiseGross + allocatedShipping + allocatedTax - totalBuyerFacingDiscount`

`merchantGrossEntitlement = buyerCashMerchantPortion + platformOrPartnerFundingPreservingMerchantEntitlement`

Merchant-funded/shop-funded discounts normally reduce merchant entitlement according to campaign contract.

At Trade level:
`sum(item buyerCashAllocation) = Trade payableAmount`

No calculation may infer funding party from coupon name after Trade creation; use snapshot.
