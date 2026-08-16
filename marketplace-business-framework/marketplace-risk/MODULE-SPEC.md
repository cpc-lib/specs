# marketplace-risk

## Responsibility
Scenario risk rules, feature snapshots, decisions, cases and anti-abuse signals.

## Marketplace V3.0 concepts mapped here
- `RiskRuleSet`
- `RiskFeatureSnapshot`
- `RiskDecision`
- `RiskCase`
- `AntiAbuse`

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
- `domain/ruleset`
- `domain/feature`
- `domain/decision`
- `domain/case`
- `domain/abuse`
- `domain/device`

## Dependency rule
This business module may depend on `marketplace-framework` modules.
It MUST NOT add a direct Maven dependency on another Marketplace business module.

Cross-domain behavior must later use:
- explicit internal API,
- published application contract,
- or event/Saga integration required by the SPEC.

## Authoritative SPEC paths
- `spec/marketplace-v3.0/02-domain/risk/`
- `spec/marketplace-v3.0/04-openapi/risk.yaml`
- `spec/marketplace-v3.0/03-database/flyway/risk/`

- `spec/marketplace-v3.0/11-codegen/`
- `spec/marketplace-v3.0/08-tests/`
- `spec/marketplace-v3.0/tasks/`

Do not copy these source files into this module.
