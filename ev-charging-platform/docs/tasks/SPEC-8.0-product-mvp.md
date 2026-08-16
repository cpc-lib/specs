# TASK — SPEC 8.0 Product MVP

## Objective

Assemble domain capabilities into authenticated Product MVP surfaces.

## Acceptance criteria

### Security

- Admin token cannot use merchant/technician/member-only semantics to bypass surface policy.
- Driver token cannot call Admin/Merchant/Technician surfaces.
- dev identity headers are disabled by default.
- internal Feign context uses a separate service key.
- station DataScope is enforced or fails closed.

### Admin

- login works with explicitly bootstrapped dev account.
- dashboard uses real domain APIs.
- no frontend `X-User-Id` spoofing remains.

### Merchant

- login and overview
- stations/orders/payments/settlement/operation pages
- station-scoped local projections do not leak tenant-wide data.

### Driver

- public station list/detail
- MEMBER login
- StartCharging / StopCharging
- order query
- payment creation

### Technician

- bearer-token login
- no normal UI use of tenant/user identity headers

## Schedule

W30-W37 / **40 person-days**, already budgeted in the 50-week Production V1 baseline.
