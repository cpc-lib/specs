# TASK — SPEC 8.2 OpenAPI + Regulatory

## Objective

Introduce a dedicated external-integration anti-corruption layer without coupling external partner/regulatory protocols into core charging domains.

## Scope

1. `charging-open` service
2. Partner credential lifecycle
3. HMAC authentication
4. nonce replay prevention
5. per-partner rate limit
6. Partner API scope and station DataScope
7. third-party charging/order API
8. signed partner callback
9. OpenAPI audit
10. regulatory adapter/report pipeline
11. outbound HTTP allowlist
12. Admin integration UI
13. OpenAPI 3.1 contract
14. E2E matrix

## Invariants

- AppSecret is encrypted at rest.
- nonce is accepted only after signature verification.
- no request can remote-start an out-of-scope connector.
- callback/report HTTP never runs inside the business transaction.
- external delivery is at-least-once and receivers must be idempotent.
- financial facts do not depend on external report success.
- the generic GB/T 44130 adapter does not claim platform certification.
- production outbound hosts are allowlisted.
- W38-W39 remains 10 person-days; total remains 50 weeks / 250 person-days.

## Exit Gate

See `docs/13-project-management/release-gates.md` and `tests/e2e/openapi-regulatory/openapi-regulatory-matrix.md`.
