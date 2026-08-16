# Merchant Credit & Level SPEC

## MerchantCreditProfile
Stores metric snapshots and computed score/version, not just a mutable integer.

Metrics:
fulfillment SLA
seller cancellation
refund/aftersale ratio
dispute ratio
complaint rate
counterfeit/IP cases
review abuse
fake shipment
service SLA
risk incidents
financial/reconciliation incidents

## MerchantLevel
Examples:
L0 / L1 / L2 / L3 / L4 or platform-defined tiers.

Level policy may affect:
settlement hold days,
campaign eligibility,
traffic eligibility,
deposit requirement,
manual review threshold,
new-category admission.

Score/level rules are effective-dated and auditable.
