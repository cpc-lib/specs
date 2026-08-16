# Marketplace security/data scope

## Platform
`PLATFORM_USER + RBAC + permission code`.

## Seller
Authenticated `MERCHANT_USER` maps to `MerchantMembership` and produces:
- merchantId
- allowed shopIds
- roles/permissions
- DataScope

Request body/query `merchantId` is never trusted for authorization.

## Buyer
buyer/user identity comes from authenticated principal. Buyer resources verify ownership.

## Service
Service-to-service authentication must be signed/mTLS/token based. Client-provided `X-Internal-*` headers are stripped at Gateway.
