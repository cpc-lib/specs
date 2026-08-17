# marketplace-settlement

## Responsibility
Merchant settlement eligibility, payable, balances, holds, payouts and adjustments.

## Marketplace V3.0 concepts mapped here
- `SettlementEligibility`
- `SettlementBatch`
- `MerchantPayable`
- `MerchantBalance`
- `MerchantFundsHold`
- `Payout`
- `SettlementAdjustment`

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
- `domain/eligibility`
- `domain/batch`
- `domain/payable`
- `domain/balance`
- `domain/payout`
- `domain/fundshold`
- `domain/adjustment`
- `domain/commission`

## Dependency rule
This business module may depend on `marketplace-framework` modules.
It MUST NOT add a direct Maven dependency on another Marketplace business module.

Cross-domain behavior must later use:
- explicit internal API,
- published application contract,
- or event/Saga integration required by the SPEC.

## Authoritative SPEC paths
- `spec/marketplace-v3.0/02-domain/settlement/`
- `spec/marketplace-v3.0/04-openapi/settlement.yaml`
- `spec/marketplace-v3.0/03-database/flyway/settlement/`

- `spec/marketplace-v3.0/11-codegen/`
- `spec/marketplace-v3.0/08-tests/`
- `spec/marketplace-v3.0/tasks/`

Do not copy these source files into this module.
