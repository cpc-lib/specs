# SPEC 8.1 Progress — Product Hardening

Status: `foundation-rc-product-hardening`

## Completed in RC

### Authentication

- access-token session ID / token ID
- short-lived access token
- rotating refresh token
- Redis-backed cross-service session revocation
- logout
- password change
- admin password reset
- account-disable session revoke
- login lockout
- independent failed-login transaction
- UTC auth-session timestamps

### RBAC

- Permission CRUD
- Role CRUD
- Role ↔ Permission assignment
- User ↔ Role assignment
- User Station DataScope assignment
- security-change audit
- session revocation after authorization changes

### API security

- `/internal-api/**` requires SERVICE identity
- production secret guard
- shared structured exception response

### DataScope

- Payment station projection
- Finance station projection
- Operation station projection
- historical projection repair
- station-scoped Merchant filtering across all current merchant domains

### Driver

- realtime ticket owner validation
- Gateway WebSocket route
- UniApp WebSocket telemetry
- polling fallback

### Frontend

- Admin/Merchant Refresh Token rotation
- Driver/Technician Refresh Token rotation
- Admin/Merchant shared loading/error state
- expanded System/RBAC UI

### E2E

- Product Hardening E2E matrix
- Product runtime smoke script

## One developer + AI estimate

SPEC 8.1 is **not a new 15-day phase added after Product MVP**.

It is the hardening half of the already allocated W30-W37 Product budget.

| Work | Person-days |
|---|---:|
| Access/Refresh/Revocation | 3 |
| Permission/Role/DataScope | 3 |
| Station projection + backfill | 3 |
| Driver WebSocket | 2 |
| Frontend resilience | 2 |
| E2E/security review/docs | 2 |
| **SPEC 8.1 subtotal** | **15** |

Product W30-W37 total remains **40 person-days**.

Production V1 remains **50 weeks / 250 person-days**.

## Remaining Product runtime gates

- Maven full compile/test
- Redis revocation E2E
- Refresh race test
- MySQL auth migration
- Station projection repair E2E
- frontend real build
- UniApp real build/device test
- WebSocket Gateway E2E
