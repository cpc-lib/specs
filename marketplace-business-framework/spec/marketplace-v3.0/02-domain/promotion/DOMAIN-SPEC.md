# Promotion & Coupon Domain SPEC
Aggregates:
- Campaign
- PromotionRule
- CouponTemplate
- CouponWallet
- CampaignBudget

Coupon states:
AVAILABLE / LOCKED / USED / EXPIRED / RELEASED

Order creation locks coupon; payment uses; cancel releases.
PromotionCompatibilityPolicy determines stackability.
Budget usage is reserved and committed.
