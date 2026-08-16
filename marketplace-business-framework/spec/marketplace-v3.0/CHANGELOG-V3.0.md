# CHANGELOG V3.0

V3.0 does not add a new business domain. It freezes and hardens V2.0-V2.6.

## Fixed
- settlement Flyway duplicate version conflict: later funds-hold migration moved from V3 to V4
- hot-family child tables receive explicit routing keys needed for binding/co-sharding
- Outbox/Inbox DDL clarified as per-service template, not shared integration database
- OpenAPI receives standard success/error response contracts and owner-service metadata
- missing command request bodies hardened for customer service, IM, review, offer publish, search reindex and selected commands
- machine-readable service/table ownership, API registry, event ownership, migration registry and sharding routes added
- frozen validation now checks Flyway version uniqueness and sharding-key availability

## Frozen
- 38 implementation tasks
- domain boundaries
- money/inventory/settlement correctness
- transactional event model
- high-concurrency/idempotency rules
- search/recommendation/CQRS read-model boundaries
