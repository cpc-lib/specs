# Merchant Domain SPEC
Aggregates:
- MerchantApplication
- Merchant
- MerchantQualification
- MerchantDepositAccount
- MerchantMembership

MerchantType:
PLATFORM_SELF / ENTERPRISE / INDIVIDUAL

Lifecycle:
REGISTERED -> IDENTITY_VERIFYING -> QUALIFICATION_REVIEW -> CONTRACT_PENDING -> ACTIVE
QUALIFICATION_REVIEW -> REJECTED
ACTIVE -> SUSPENDED -> ACTIVE/CLOSED

Invariants:
- legal identity is versioned and audited
- settlement account changes require risk review
- C2C seller must complete identity verification
- merchant deposit is liability/security balance, not platform revenue
