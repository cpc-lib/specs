# Brand Authorization SPEC

Aggregate: BrandAuthorization

Scope:
merchant + brand + category(optional) + shop(optional)

Fields:
authorizationNo, authorizationType, proofFileIds, issuer,
effectiveFrom, effectiveTo, status, reviewWorkflowId, version.

States:
DRAFT -> PENDING_REVIEW -> ACTIVE
PENDING_REVIEW -> REJECTED
ACTIVE -> EXPIRED / REVOKED

Publishing policy can require ACTIVE authorization.
Revocation blocks new Offer publication/sale according to policy but does not rewrite historical orders.

Sensitive proof files use secure File Service and audit.
