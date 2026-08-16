# Prohibited / Restricted Goods Governance SPEC

## ProductGovernancePolicy
Classification:
ALLOWED
PROHIBITED
RESTRICTED
LICENSE_REQUIRED
AGE_RESTRICTED
REGION_RESTRICTED

Dimensions:
category / attribute / keyword / brand / region / seller type / risk level.

## Evaluation
Publish-time:
hard prohibition -> REJECT/BLOCK.
Restricted -> require qualification/authorization.

Checkout-time:
region/age/current policy revalidation when required.

Post-sale:
policy enforcement may stop new sales but must preserve historical order facts and continue refund/aftersale obligations.
