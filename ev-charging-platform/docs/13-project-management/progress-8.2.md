# SPEC 8.2 Progress — OpenAPI + Regulatory

Status: `foundation-rc-openapi-regulatory`

## Standards baseline

Verified current on 2026-08-10:

- GB/T 44130.1-2024
- GB/T 44130.2-2025
- GB/T 44130.3-2025
- GB/T 44130.4-2025
- GB/T 44130.5-2025

Parts 2-5 of the 2025 series were implemented on 2026-03-01.

## RC scope completed

- independent `charging-open` service
- Partner AppKey/AppSecret
- AES-256-GCM secret-at-rest
- HMAC-SHA256 canonical request
- timestamp / nonce replay protection
- Redis rate limit
- OpenAPI audit
- Partner Scope
- Partner Station DataScope
- connector → station authorization
- partner external-user shadow mapping
- partner remote start/stop
- partner order read
- signed outbound callback
- callback claim/retry/dead/recovery
- regulatory platform registry
- regulatory snapshot tasks
- GB/T 44130 canonical adapter boundary
- regulatory claim/retry/dead/recovery
- outbound host allowlist
- Admin OpenAPI/Regulatory page
- OpenAPI 3.1 contract
- E2E matrix

## Schedule

SPEC 8.2 maps exactly to existing **W38-W39 / 10 person-days**.

| Work | Person-days |
|---|---:|
| HMAC / nonce / rate limit / secret storage | 2 |
| Partner OpenAPI + Station Scope | 2 |
| Callback / Audit / recovery | 1.5 |
| Regulatory adapter / report pipeline | 2 |
| Admin UI / API contract | 1 |
| E2E / security review / docs | 1.5 |
| **Total** | **10** |

Production V1 remains **50 weeks / 250 person-days**.

No integration buffer is consumed.
