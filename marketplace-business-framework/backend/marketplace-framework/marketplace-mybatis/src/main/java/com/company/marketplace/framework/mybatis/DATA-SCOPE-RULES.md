# Data scope rules
- Platform APIs: RBAC and explicit platform permissions.
- Seller APIs: merchantId is derived from `MarketplacePrincipal`.
- Shop-scoped rows: shopId must be inside authenticated shop scope.
- Buyer rows: userId/buyerId comes from authenticated principal.
- Request `merchantId` is never an authorization source.

The first framework intentionally does not inject opaque SQL automatically. Repositories/application queries must explicitly apply scope predicates; later a proven MyBatis DataPermission interceptor may be added without changing domain modules.
