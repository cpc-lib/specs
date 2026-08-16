# Coupon SPEC

## CouponTemplate
Fields:
couponType, ownerType, ownerId,
claimStart/End, useStart/End,
thresholdAmount, benefit,
totalIssueLimit, perUserClaimLimit, perUserUseLimit,
scope/exclusion, compatibilityPolicy, fundingParty, status, version.

## CouponWallet
One issued coupon instance.

States:
AVAILABLE -> LOCKED -> USED
AVAILABLE -> EXPIRED
LOCKED -> RELEASED / USED

## Claim
Claim quota and usage quota are different concepts.
High concurrency claim:
Redis/Lua may perform front quota reservation
→ durable claim reservation
→ create CouponWallet
→ commit/release quota
→ reconciliation.

## Use
CreateTrade Saga locks coupon with tradeNo.
Payment success commits USED.
Trade cancellation releases if still locked and policy permits.

## Invariants
- one coupon instance cannot be USED twice
- claim/use limits are enforced independently
- coupon currency/scope must match
- coupon funding responsibility is snapshotted
