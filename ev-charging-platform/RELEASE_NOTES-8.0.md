# Release Notes — SPEC 8.0 Product MVP

Status: `foundation-rc-product-mvp`

## Added

- HMAC-signed Product-MVP access token
- AccessPrincipal / roles / permissions / DataScope
- surface role enforcement
- internal service context
- System IAM schema + dev bootstrap switch
- Admin login/dashboard/system page
- Merchant authenticated portal
- Driver station/charging/order/payment journey
- Technician bearer-token authentication

## Secure-default changes

- `DEV_TENANT_HEADER_ENABLED=false` by default
- `BOOTSTRAP_DEMO_USERS=false` by default
- demo accounts require explicit enablement
- no Admin/Merchant/Driver/Technician frontend uses `X-Tenant-Id` or `X-User-Id`

## Schedule

No total schedule change.

Product MVP consumes the already planned **W30-W37 / 40 person-days**.

Production V1 remains **50 weeks / 250 person-days**.
