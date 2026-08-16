# Release Notes — SPEC 8.2 OpenAPI + Regulatory

Status: `foundation-rc-openapi-regulatory`

## New service

- `charging-open`
- port 8088
- Partner OpenAPI
- Partner Admin
- Regulatory Integration

## Partner security

- AppKey / encrypted AppSecret
- AES-256-GCM secret storage
- HMAC-SHA256 canonical request
- timestamp skew
- Redis nonce anti-replay
- Redis requests/minute rate limit
- request audit
- AppSecret rotation
- independent Callback Secret

## Partner API

- station list/detail
- remote charging start/stop
- partner-owned order query
- external user shadow mapping
- Connector → Station scope validation

## Callback

- signed callbacks
- persistent task
- claim token
- RETRY / DEAD
- stale claim recovery
- manual retry

## Regulatory

- platform registry
- public-station snapshot
- business-order snapshot
- payload hash idempotency
- `RegulatoryProtocolAdapter`
- `GB_T_44130_2025_CANONICAL`
- claim/retry/dead/recovery
- per-platform rate limit

The GB/T adapter is explicitly marked `canonical-adapter-not-platform-certified`.

## Security

- production outbound host allowlist
- production HTTPS-only outbound calls
- private/loopback literal denial in production
- URL revalidation before HTTP dispatch
- production OpenAPI master-key guard

## Cross-slice fix

Driver/native Member charging now uses the authenticated `RequestContext.requireUserId()` rather than the previous development constant user ID.

## Schedule

SPEC 8.2 consumes the planned **W38-W39 / 10 person-days**.

Production V1 remains **50 weeks / 250 person-days**.
