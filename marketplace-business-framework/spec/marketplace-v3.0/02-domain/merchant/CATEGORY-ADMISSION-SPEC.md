# Merchant Category Admission SPEC

`MerchantCategoryAdmission` scopes seller eligibility:
merchantId, shopId(optional), categoryId, region,
requiredQualificationsSnapshot, approvalStatus,
effectiveFrom/effectiveTo, riskRestrictions, version.

States:
DRAFT -> PENDING_REVIEW -> ACTIVE
PENDING_REVIEW -> REJECTED
ACTIVE -> SUSPENDED / EXPIRED / REVOKED

Offer publish and checkout/saleability revalidate admission according to policy.
Historical OrderItem snapshots remain valid.
