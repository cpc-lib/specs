# V3.0 Frozen Baseline Release Gates

## Repository
- all YAML/JSON parse
- all OpenAPI operationIds unique
- all OpenAPI version = 3.0
- every operation has owner/auth/idempotency metadata
- structured success/error responses
- all path variables declared

## Flyway
- version unique per service
- no duplicate CREATE TABLE logical names
- frozen route key exists in each configured binding table
- settlement migration conflict resolved
- Outbox explicitly template/per-service

## Events
- event registry schemas exist
- event schema eventType matches registry key
- producer owner is defined
- transactional events use versioned schema

## Correctness
- money invariants preserved
- inventory no oversell
- refund/payout quota protection
- UNKNOWN semantics preserved
- merchant isolation fail closed
- search/recommendation/CQRS never source of financial truth

## Codegen
- exactly 38 frozen TASKs
- all TASKs reference current MASTER
- each task has frozen bundle context
- E2E traceability exists
- no unresolved P0 freeze issue

## External validation still required after generated code exists
- live MySQL migration execution
- Testcontainers suites
- real ShardingSphere integration
- payment/provider sandbox
- load/performance tests
- security testing
- jurisdiction-specific legal/compliance review
