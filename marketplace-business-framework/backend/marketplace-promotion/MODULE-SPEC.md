# marketplace-promotion

## Responsibility
Campaigns, coupons, promotion budgets/quotas, gifts, bundles and flash sales.

## Marketplace V3.0 concepts mapped here
- `PromotionCampaign`
- `PromotionRule`
- `Coupon`
- `CampaignBudget`
- `PromotionQuota`
- `FlashSale`
- `Gift`
- `Bundle`

## DDD skeleton
- `interfaces/rest`
- `application/command`
- `application/query`
- `application/service`
- `domain/model`
- `domain/repository`
- `domain/service`
- `domain/policy`
- `domain/event`
- `infrastructure/persistence`
- `infrastructure/config`

## Domain slices
- `domain/campaign`
- `domain/coupon`
- `domain/budget`
- `domain/quota`
- `domain/compatibility`
- `domain/flashsale`
- `domain/gift`
- `domain/bundle`
- `domain/purchaselimit`

## Dependency rule
This business module may depend on `marketplace-framework` modules.
It MUST NOT add a direct Maven dependency on another Marketplace business module.

Cross-domain behavior must later use:
- explicit internal API,
- published application contract,
- or event/Saga integration required by the SPEC.

## Authoritative SPEC paths
- `spec/marketplace-v3.0/02-domain/promotion/`
- `spec/marketplace-v3.0/04-openapi/promotion.yaml`
- `spec/marketplace-v3.0/03-database/flyway/promotion/`

- `spec/marketplace-v3.0/11-codegen/`
- `spec/marketplace-v3.0/08-tests/`
- `spec/marketplace-v3.0/tasks/`

Do not copy these source files into this module.
