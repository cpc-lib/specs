# Product Hardening E2E Matrix — SPEC 8.1

## AUTH-E2E-001 Login / Refresh Rotation / Revocation

1. Enable demo users explicitly.
2. Login as `admin`.
3. Call `/auth-api/v1/me` with access token → 200.
4. Refresh once → new access + new refresh.
5. Reuse old refresh token → must fail.
6. Logout with new access token.
7. Reuse new access token on another service → must return 401 because Redis session revocation is cross-service.

Acceptance:

- one refresh token can be successfully rotated only once
- logout invalidates the whole session
- no service may accept a revoked session token

## AUTH-E2E-002 Password / Account Lifecycle

1. Login as driver.
2. Change password.
3. Old access session becomes revoked.
4. Old password fails.
5. New password succeeds.
6. Admin disables driver.
7. All driver sessions become revoked.
8. Login while disabled fails.
9. Admin re-enables account.

## AUTH-E2E-003 Login Lockout

1. Submit five wrong passwords for the same account.
2. `failed_login_count` must persist after each failure.
3. Fifth failure sets `locked_until`.
4. Correct password during lock period must still fail.
5. Verify failure audit rows exist.

This specifically guards against the transactional rollback bug fixed in SPEC 8.1.

## RBAC-E2E-001 API Surface Isolation

| Principal | Admin API | Merchant API | Technician API | Member charging API |
|---|---:|---:|---:|---:|
| ADMIN | allow | allow | allow | allow |
| MERCHANT | deny | allow | deny | deny |
| MERCHANT_STATION | deny | allow | deny | deny |
| TECHNICIAN | deny | deny | allow | deny |
| MEMBER | deny | deny | deny | allow |
| SERVICE | deny | deny | deny | deny |

SERVICE is allowed only on `/internal-api/**`.

## RBAC-E2E-002 Internal API Protection

- anonymous `/internal-api/**` → 401
- MEMBER bearer token → 403
- ADMIN bearer token → 403
- valid internal service key + tenant context → allow
- invalid service key → 401

## SCOPE-E2E-001 Station DataScope — Full Merchant Projection

Create Station A and Station B.

Create a `MERCHANT_STATION` user scoped only to Station A.

Generate business at both stations.

The user must see only Station A in:

- Asset station list
- Core charging/order projection
- Payment projection
- Finance settlement-source projection
- Operation alarm/work-order projection

Historical records created before SPEC 8.1 must become visible only after authoritative projection-repair jobs backfill `station_id`.

## WS-E2E-001 Driver Realtime Ownership

1. Driver A starts Session A.
2. Driver B attempts to issue realtime ticket for Session A → deny/not found.
3. Driver A gets one-time ticket.
4. Ticket connects `/ws/charging`.
5. Reusing same ticket fails.
6. Telemetry arrives over WebSocket.
7. Disconnect WebSocket and verify Driver app falls back to polling.
8. Reconnect and verify new ticket works.

## FIN-E2E-001 Maker-Checker With Real Identity

1. Login `admin`.
2. Create/submit financial approval action.
3. Same user cannot approve own action where maker-checker rule applies.
4. Login `admin2`.
5. Approve successfully.
6. Verify audit contains real user IDs; no `X-User-Id` spoofing exists.

## UX-E2E-001 Product Error States

Simulate:

- 401 access-token expiry → transparent refresh
- refresh failure → login screen
- 403 DataScope → user-visible permission error
- 409 business conflict → user-visible conflict
- dashboard partial service failure → error state, not blank page
- WebSocket error → polling fallback
