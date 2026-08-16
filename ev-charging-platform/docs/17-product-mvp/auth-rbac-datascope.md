# Authentication / RBAC / DataScope

## Identity paths

Three identity channels are deliberately separated.

### End-user access token

Used by:

- Admin
- Merchant
- Driver
- Technician

Header:

`Authorization: Bearer <signed-token>`

SPEC 8.0 uses a HMAC-SHA256 signed Product-MVP token containing:

- tenantId
- userId
- username
- roles
- permissions
- dataScopeType
- stationIds
- expiration

Production Hardening may replace this codec with Spring Security OAuth2 Resource Server / centralized IAM without changing `AccessPrincipal`.

### Internal service context

Service-to-service Feign uses:

- `X-Service-Key`
- `X-Internal-Tenant-Id`
- `X-Internal-User-Id`
- `X-Request-Id`

It does not reuse browser/user tokens.

### Development identity headers

`X-Tenant-Id` / `X-User-Id` are disabled by default.

They require explicit:

`DEV_TENANT_HEADER_ENABLED=true`

and are not the Product-MVP login mechanism.

## Surface roles

| API surface | Allowed role |
|---|---|
| `/admin-api/**` | ADMIN |
| `/merchant-api/**` | MERCHANT / MERCHANT_STATION |
| `/technician-api/**` | TECHNICIAN |
| protected `/app-api/**` | MEMBER |
| `/internal-api/**` | internal service context |

ADMIN is permitted as an operational override in development/admin flows.

## DataScope

Supported:

- ALL
- TENANT
- STATION
- SELF

Station scope is currently implemented for:

- Merchant Station list
- Merchant Core charging/order projection

Payment / Finance / Operation merchant projections do not yet persist stationId in their local read models. For `STATION` DataScope these APIs **reject access** instead of returning tenant-wide data.

This fail-closed behavior is mandatory.

## Demo bootstrap

Demo users are **not** created by default.

Explicitly enable:

`BOOTSTRAP_DEMO_USERS=true`

Development accounts:

- admin / admin123456
- admin2 / admin2123456
- merchant / merchant123456
- technician / tech123456
- driver / driver123456

These credentials must never be enabled in production.
