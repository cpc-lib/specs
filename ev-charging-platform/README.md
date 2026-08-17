# EV Charging Platform — SPEC 8.3 Security + Performance + Chaos RC

Status: **`foundation-rc-security-performance-chaos`**

Production V1 is now in W40-W42 hardening.

## Stability stack

```text
Gateway Sentinel
→ MVC Sentinel hot paths
→ bounded Tomcat / Executor / Hikari
→ finite Feign / MQ timeouts
→ MySQL transaction / Outbox
→ Prometheus SLO
→ Chaos recovery gates
```

## Capacity baseline

- 10,000 online devices
- 2,000 concurrent charging sessions
- 5,000 telemetry messages/second

These are workload targets, not unsupported single-instance benchmark claims.

## Schedule

SPEC 8.3 = **W40-W42 / 15 person-days**.

Production V1 remains **50 weeks / 250 person-days**.

## SPEC 7.7 hardening

### Finance controls

- T+1 reconciliation schedule by tenant/channel/merchant/timezone.
- Raw normalized channel bill archived with SHA-256 before import.
- Payment/Refund facts remain immutable.
- Adjustment and Reversal are append-only signed facts.
- Reconciliation persists original amount + adjustment amount + effective amount.
- `¥100.00 != ¥99.99` remains an exact mismatch.
- Only `MATCH` may create Settlement Source.
- Settlement calculation creates `PENDING_APPROVAL`, not immediate settlement.
- Maker and checker must be different users for human-created batches.
- Approval posts a balanced Ledger transaction before Source becomes `SETTLED`.
- Invoice Provider calls run outside DB transactions.
- Mock Invoice Provider supports issue / retry / red flush for development.

### Project schedule

The one-person + AI production plan is part of the repository:

- `PROJECT_PLAN.md`
- `docs/13-project-management/one-person-ai-development-plan.md`
- `docs/13-project-management/sprint-plan.md`
- `docs/13-project-management/progress-7.9.md`

Current baseline: **50 weeks / 250 person-days**.

## Finance documents

- `docs/09-finance/finance-hardening-7.7.md`
- `docs/09-finance/adjustment-reversal.md`
- `docs/09-finance/settlement-approval.md`
- `docs/09-finance/invoice.md`
- `docs/09-finance/channel-bill-normalized-example.json`

## Verification honesty

This package is still an RC. Static validation, JDK syntax parsing, pure-Java finance harnesses,
JDBC placeholder checks and structured-file parsing can be run in the current environment.

It must **not** be labelled `foundation-verified` until the runtime release gate passes:

- `cd backend && mvn clean verify`
- MySQL + all Flyway migrations
- Testcontainers
- Kafka payment → finance consumers
- Docker Compose
- Nacos registration/discovery
- frontend `npm install && npm run build`
- live reconciliation / settlement approval E2E

## SPEC 7.9 — Operation Hardening

Key assets:

- `backend/charging-iot/src/main/java/com/example/evcharging/iot/lifecycle`
- `backend/charging-operation/src/main/java/com/example/evcharging/operation/notification`
- `backend/charging-operation/src/main/java/com/example/evcharging/operation/inspection`
- `backend/charging-operation/src/main/java/com/example/evcharging/operation/spare`
- `backend/charging-operation/src/main/java/com/example/evcharging/operation/attachment`
- `technician-app/`
- `docs/16-operation-hardening/`
- `docs/13-project-management/progress-7.9.md`


## SPEC 8.0 docs

- `docs/17-product-mvp/product-mvp.md`
- `docs/17-product-mvp/auth-rbac-datascope.md`
- `docs/17-product-mvp/admin-web.md`
- `docs/17-product-mvp/merchant-web.md`
- `docs/17-product-mvp/driver-uniapp.md`
- `docs/13-project-management/progress-8.0.md`


## SPEC 8.1 docs

- `docs/17-product-mvp/product-hardening-8.1.md`
- `docs/17-product-mvp/token-session-security.md`
- `docs/17-product-mvp/station-datascope-projection.md`
- `tests/e2e/product-hardening/product-e2e-matrix.md`
- `docs/13-project-management/progress-8.1.md`


## SPEC 8.2 docs

- `docs/18-openapi/openapi-auth.md`
- `docs/18-openapi/partner-api.md`
- `docs/18-openapi/callback.md`
- `docs/18-openapi/regulatory-standard-baseline.md`
- `docs/18-openapi/regulatory-integration.md`
- `docs/18-openapi/outbound-security.md`
- `docs/18-openapi/openapi-v1.yaml`
- `tests/e2e/openapi-regulatory/openapi-regulatory-matrix.md`
- `docs/13-project-management/progress-8.2.md`


## SPEC 8.3 docs

- `docs/19-hardening/security-performance-chaos-8.3.md`
- `docs/19-hardening/slo-and-capacity.md`
- `docs/19-hardening/sentinel-and-backpressure.md`
- `docs/19-hardening/key-rotation.md`
- `docs/19-hardening/chaos-testing.md`
- `tests/performance/README.md`
- `tests/chaos/chaos-matrix.md`
- `docs/13-project-management/progress-8.3.md`
