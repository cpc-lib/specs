# Marketplace Business Framework v1.2 — SPEC Integrated

This repository combines:

- lightweight enterprise Spring Cloud business framework,
- 26 top-level Marketplace business modules,
- the complete Marketplace V3.0 Frozen SPEC.

## Most important change from v1.1

The complete frozen specification is now inside this repository:

```text
spec/
└── marketplace-v3.0/
```

There is **no `spec-contract/` directory** in v1.2.

## Start here

1. `SPEC-ENTRYPOINT.md`
2. `spec/marketplace-v3.0/00-master/MASTER-SPEC-V3.0.md`
3. `docs/MARKETPLACE-BUSINESS-MODULES.md`
4. the target service's `MODULE-SPEC.md`

## Repository structure

```text
marketplace-business-framework-v1.2-spec-integrated/
├── pom.xml
├── spec/
│   ├── VERSION
│   ├── README.md
│   └── marketplace-v3.0/
├── marketplace-dependencies/
├── marketplace-framework/
├── marketplace-gateway/
├── marketplace-system/
├── marketplace-user/
├── marketplace-merchant/
├── marketplace-shop/
├── marketplace-product/
├── marketplace-pricing/
├── marketplace-inventory/
├── marketplace-promotion/
├── marketplace-cart/
├── marketplace-checkout/
├── marketplace-trade/
├── marketplace-payment/
├── marketplace-fulfillment/
├── marketplace-aftersale/
├── marketplace-dispute/
├── marketplace-settlement/
├── marketplace-finance/
├── marketplace-reconciliation/
├── marketplace-invoice/
├── marketplace-review/
├── marketplace-risk/
├── marketplace-governance/
├── marketplace-search/
├── marketplace-recommendation/
├── marketplace-notification/
├── marketplace-customer-service/
└── marketplace-cqrs/
```

## SPEC rule

The full SPEC is maintained only once under `spec/marketplace-v3.0/`.

Individual service modules contain only implementation mapping documents; they must not
copy the whole specification.

---

## V1.1 framework notes retained

# Marketplace Business Framework v1.1 Complete

This is the corrected **basic business framework** for the Marketplace V3.0 SPEC.

## What changed from v1.0

V1.0 technically contained 26 modules, but they were hidden under `marketplace-services/`
and many were only minimal placeholders.

**V1.1 fixes that. All 26 business modules are top-level directories and have a real,
ready-to-fill DDD development skeleton.**

## Scope

Included:
- Java 21 / Spring Boot / Spring Cloud basic Maven structure
- lightweight Framework modules
- Gateway
- System/RBAC foundation
- Merchant/Shop security scope foundation
- MyBatis-Plus / Redis / Redisson / MinIO foundation
- 26 Marketplace business modules
- DDD four-layer package skeleton in every business module
- Spring Boot service entrypoint in every business module
- application.yml and migration directory in every business module
- Marketplace V3.0 concept-to-module mapping

Not implemented in this framework stage:
- concrete Product/Trade/Payment business use cases
- full RabbitMQ/Kafka platform
- full observability platform
- recommendation models
- advertising/data-platform business

## First place to inspect

`docs/MARKETPLACE-BUSINESS-MODULES.md`

It lists all 26 business modules explicitly.

## Rule

The Marketplace V3.0 SPEC is the business source of truth.
This repository is the implementation skeleton; it does not replace the SPEC.

## Project Navigation

### Business specification

- `SPEC-ENTRYPOINT.md`
- `spec/marketplace-v3.0/`

### Project execution

- `docs/project-management/MASTER-ROADMAP.md`
- `docs/project-management/Marketplace全量实现_单人AI_24个月计划.md`
- `docs/project-management/MILESTONES.md`
- `docs/project-management/RELEASE-PLAN.md`
- `docs/project-management/PROGRESS.md`

The SPEC defines **what the system must do**.  
Project-management documents define **when and in what order it is implemented**.

## Frontend Applications

本仓库正式包含三套前端：

```text
marketplace-platform-web/  React 平台管理后台
marketplace-seller-web/    React 商家后台
marketplace-buyer-app/     UniApp 买家端
```

它们与 `spec/marketplace-v3.0/` 和 26 个后端业务模块共同构成 Marketplace V1.0。
