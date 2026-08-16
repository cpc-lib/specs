# TASK-010 — Promotion Coupon

## Goal
Implement `Promotion Coupon` according to Marketplace MASTER SPEC V3.0.

## Scope
campaign/coupon/budget

## Mandatory Inputs
- `00-master/MASTER-SPEC-V3.0.md`
- relevant `02-domain/**`
- relevant `03-database/flyway/**`
- relevant `04-openapi/**`
- `05-events/event-registry.yaml`
- `10-registries/**`
- `08-tests/**`

## Required Deliverables
1. DDD package tree
2. full Java 21 code
3. Flyway integration
4. REST/Internal APIs
5. event producer/consumer
6. idempotency
7. permission/data isolation
8. audit/metrics
9. unit/integration/contract tests
10. Testcontainers where applicable
11. README
12. SPEC implementation mapping

## Red Lines
- no TODO/pseudocode
- no client authoritative merchantId
- no float/double money
- no direct status setter endpoints
- no MySQL+ES dual write
- no duplicate money effects
- no Redis-only normal inventory truth
- no MQ consumer without inbox where transactional

## Definition of Done
All relevant contract, concurrency, isolation and failure-path tests pass.

## V2.2 Transaction & Finance Requirements
This task must consume relevant V2.2 funding, clearing, refund reversal, merchant payable, payout, accounting and reconciliation contracts. Do not re-derive economic responsibility from mutable promotion rules after Trade creation.

## V2.4 Mandatory
Implement PromotionRule/Scope/Compatibility, budget/quota reservation,
CouponTemplate/claim/use limits, deterministic benefit selection, funding allocation and snapshots.

## V3.0 Frozen Codegen Contract
This TASK is implemented against the V3.0 Frozen Baseline.
Breaking domain/API/event/sharding changes require ADR + baseline revision.
Generated deliverable must include `SPEC-IMPLEMENTATION-MAP.md`.
