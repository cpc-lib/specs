# Pricing Engine SPEC V2.4

## Inputs
- offerId / skuId
- buyer/member level
- region
- sales channel
- quantity
- evaluation time
- merchant/shop status
- active PriceBook versions

## Models
- PriceBook
- PriceBookItem
- PriceRule
- PricingResult
- PricingSnapshot

## Price Dimensions
BASE / REGION / CHANNEL / MEMBER / CONTRACTUAL / FLASH

## Candidate Selection
Hard match dimensions first.
Precedence default:
FLASH > explicit contractual/approved special > member > channel/region > sale > base.
Actual precedence is versioned by `PriceSelectionPolicy`.

Tie-break:
1 priority desc
2 specificity desc
3 effectiveFrom desc
4 rule/price id asc

## Snapshot
Stores:
basePrice
selectedPrice
candidate references
policy version
currency
region/channel/member inputs
calculation trace hash/full trace ref

## Rules
- amount >= 0
- currency consistent
- effective interval `[from,to)`
- overlapping prices at same specificity/priority are rejected at publish unless policy explicitly supports it
- SubmitTrade always recomputes and compares checkout snapshot/version.
