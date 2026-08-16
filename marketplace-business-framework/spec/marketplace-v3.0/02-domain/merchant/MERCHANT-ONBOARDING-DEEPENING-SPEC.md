# Merchant Onboarding Deepening SPEC

## Aggregates
- MerchantApplication
- MerchantVerificationProfile
- Merchant
- MerchantSettlementProfile
- MerchantQualification
- MerchantCategoryAdmission

## Enterprise Application
Required business artifacts vary by region/policy:
legal entity name/registration,
representative/contact,
beneficial ownership reference when needed,
business address,
settlement account proof,
tax profile,
category licenses,
brand authorizations.

## Individual C2C
Identity verification plus:
age eligibility,
account/device risk,
payout profile verification,
restricted category policy.

## Application State
DRAFT -> SUBMITTED -> IDENTITY_REVIEW -> QUALIFICATION_REVIEW -> RISK_REVIEW
-> AGREEMENT_PENDING -> DEPOSIT_PENDING(optional) -> APPROVED -> ACTIVATING -> ACTIVE

Alternatives:
REJECTED / WITHDRAWN / EXPIRED.

## Invariants
- applicant identity cannot be silently replaced after approval.
- settlement account change after ACTIVE is a separate high-risk change workflow.
- approval references exact verification/qualification/risk versions.
- cross-merchant evidence reference forbidden.
- all sensitive identity fields encrypted/masked and access audited.
