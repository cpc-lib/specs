# Promotion Engine SPEC V2.4

## Models
- PromotionCampaign
- PromotionRule
- PromotionScope
- PromotionCompatibilityRule
- PromotionBudgetAccount
- PromotionBudgetReservation
- PromotionQuota
- PromotionQuotaReservation
- PromotionCalculationSnapshot

## Eligibility
Inputs can include:
buyer segment/member,
merchant/shop/category/brand/offer/SKU,
region/channel,
quantity,
goods amount,
time,
purchase history,
risk decision.

## Rule Types
DIRECT_DISCOUNT
PERCENTAGE_DISCOUNT
FULL_REDUCTION
FULL_DISCOUNT
N_FOR_FIXED_PRICE
N_ITEMS_DISCOUNT
BUY_X_GET_Y
GIFT
BUNDLE
MEMBER_PRICE
FLASH_SALE
FREE_SHIPPING

## Application
Rules produce BenefitCandidate:
- monetary discount
- price override
- free quantity/gift
- shipping discount
- entitlement.

Then compatibility engine selects valid combination.

## Deterministic Selection
For mutually exclusive candidates:
maximize buyer benefit unless policy says fixed priority.
Tie-break:
priority desc → campaign start asc → campaignId asc → ruleId asc.

## Funding
Every monetary benefit outputs FundingAllocation:
PLATFORM / MERCHANT / SHOP / BRAND / PARTNER.
Display owner and funding party may differ.

## Snapshot
Selected and rejected candidate references can be retained for explainability.
Selected rule versions and calculation trace are immutable in Trade.
