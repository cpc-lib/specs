# Product Domain SPEC
Models:
- PlatformCategory
- Brand
- CategoryAttribute
- SPU
- SKU
- Offer
- ProductVersion

Offer is the merchant-selling unit.
Lifecycle:
DRAFT -> PENDING_REVIEW -> APPROVED -> ONLINE -> OFFLINE
ONLINE -> BLOCKED

OrderItem must snapshot product title/image/specs/merchant/shop/policy.
No online product core edit without versioning/audit.
