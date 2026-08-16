# Release Notes — SPEC 8.1 Product Hardening

Status: `foundation-rc-product-hardening`

## Security

- short-lived Access Token
- rotating Refresh Token
- Redis-backed cross-service revocation
- password change/reset revocation
- login lockout
- Permission CRUD
- Role CRUD
- Station DataScope management
- protected `/internal-api/**`
- production secret startup guard

## DataScope

- Payment station projection
- Finance station projection
- Operation station projection
- historical projection-repair jobs

Merchant Station scope now has local database filtering across all current merchant read surfaces.

## Driver

- owner-bound realtime ticket
- Gateway WebSocket route
- UniApp realtime telemetry
- polling fallback

## Frontend

- Admin/Merchant automatic Refresh Token
- Driver/Technician automatic Refresh Token
- shared page loading/error state
- expanded RBAC management UI

## Schedule

No total increase.

SPEC 8.1 consumes **15 person-days inside W30-W37 Product MVP**.

Production V1 remains **50 weeks / 250 person-days**.
