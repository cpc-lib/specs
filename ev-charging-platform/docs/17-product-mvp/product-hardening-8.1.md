# SPEC 8.1 — Product Hardening

Status: `foundation-rc-product-hardening`

SPEC 8.1 hardens the Product MVP from SPEC 8.0 without adding a new business bounded context.

## 1. Identity lifecycle

```text
Login
  ↓
Access Token (15 min)
+
Refresh Token (30 days)
  ↓
Refresh rotation
  ↓
Logout / Password Change / Account Disable / Role Change
  ↓
Session revoked in MySQL
+
Redis revoked-session key
  ↓
All user-facing microservices reject the revoked Access Token
```

### Access token claims

- `jti` — access-token ID
- `sid` — authentication session ID
- tenantId
- userId
- roles
- permissions
- DataScope
- stationIds
- expiration

### Refresh token

Refresh tokens are random 256-bit values.

Only SHA-256 hashes are persisted.

A successful refresh rotates the token atomically:

`old refresh → invalid`

This prevents concurrent replay of the old refresh token.

## 2. Session revocation

Revocation triggers:

- logout
- password change
- administrator password reset
- account disable
- role assignment change
- station scope change
- role permission/DataScope change

`TokenRevocationChecker` is located in the shared WebMVC framework.

It checks Redis on every user Bearer-token request.

If the revocation store is unavailable, authentication fails closed.

## 3. Login lockout

Five failed password attempts cause a 15-minute lock.

Failed-login accounting uses a dedicated `REQUIRES_NEW` transaction.

This is necessary because throwing a security exception from the main login flow must not roll back failed-attempt persistence.

## 4. RBAC hardening

Admin can now manage:

- users
- user status
- password reset
- roles
- role permissions
- permission catalog
- user-role assignment
- station scope

Role/DataScope/security changes revoke affected active sessions.

## 5. Internal API hardening

`/internal-api/**` is now an authenticated API surface.

Only role:

`SERVICE`

is accepted.

Normal ADMIN / MERCHANT / TECHNICIAN / MEMBER access tokens cannot invoke internal APIs.

Service calls use:

- X-Service-Key
- X-Internal-Tenant-Id
- X-Internal-User-Id
- X-Request-Id

## 6. Full Merchant Station DataScope

`station_id` is now projected locally into:

- Payment
- Finance
- Operation

Combined with existing Asset/Core station data, Merchant Station scope covers:

```text
Asset
Core
Payment
Finance
Operation
```

No browser-side filtering is trusted.

## 7. Historical projection repair

SPEC 8.1 is upgrade-safe.

Historical rows with null `station_id` are repaired asynchronously using authoritative internal APIs.

Repair jobs:

- PaymentStationProjectionRepairJob
- FinanceStationProjectionRepairJob
- OperationStationProjectionRepairJob

Until repaired, station-scoped access remains fail-closed because null station rows do not match a station filter.

## 8. Driver realtime

Realtime ticket issuance now verifies:

`tenantId + userId + sessionNo`

A tenant member cannot issue a ticket for another member's charging session.

Driver UniApp flow:

```text
Issue one-time ticket
→ WebSocket
→ realtime telemetry
→ connection failure
→ polling fallback
```

## 9. Frontend resilience

Admin and Merchant now have reusable loading/error/empty states.

HTTP clients implement:

- request ID
- transparent refresh
- single-flight refresh
- one retry after access expiration
- logout only after refresh failure

Driver and Technician clients implement equivalent Refresh Token rotation.
