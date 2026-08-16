# ADR-008 — External Integration Anti-Corruption Layer

Status: Accepted in SPEC 8.2.

## Decision

Create a dedicated `charging-open` service.

Do not expose Asset/Core/Payment/Finance domain services directly to third-party Partners or regulatory systems.

## Reasons

External protocols have independent concerns:

- credentials
- replay protection
- rate limiting
- external idempotency
- callback delivery
- protocol versions
- regulatory field codes
- audit
- outbound egress security
- partner-specific DataScope

Placing these inside Core would couple charging state machines to external protocol churn.

## Boundary

```text
External Party
    ↓
charging-open
    ↓ stable internal contracts
Asset / Core / Payment / Finance / Operation
```

## Consequences

Positive:

- protocol replacement does not rewrite charging domain
- local/provincial regulatory profiles become adapters
- partner security has one enforcement point
- external retry failure is isolated

Trade-offs:

- one extra service and database schema
- additional read-model/projection contracts
- eventual consistency for callbacks/regulatory reporting
