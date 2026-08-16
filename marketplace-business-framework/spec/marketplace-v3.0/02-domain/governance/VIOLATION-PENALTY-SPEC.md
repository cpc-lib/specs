# Violation & Penalty SPEC

## ViolationCase
Fields:
violationNo
subjectType / subjectId
merchantId / shopId
violationType
severity
ruleCode / ruleVersion
evidenceSnapshotRef
sourceType / sourceId
status
reviewer / decidedAt
decisionSummary
version

States:
OPEN -> INVESTIGATING -> PENDING_DECISION -> CONFIRMED / DISMISSED
CONFIRMED -> PENALTY_EXECUTING -> RESOLVED -> CLOSED
Any eligible confirmed state -> APPEALED.

## PenaltyAction
Action types:
WARNING
PRODUCT_TAKE_DOWN
PRODUCT_BLOCK
SHOP_LIMIT
SHOP_SUSPEND
MERCHANT_SUSPEND
DEPOSIT_FREEZE
DEPOSIT_DEDUCTION
FUNDS_HOLD
SETTLEMENT_DELAY
CAMPAIGN_RESTRICTION
TRAFFIC_RESTRICTION
ACCOUNT_TERMINATION

Penalty execution is idempotent:
violationId + actionType + actionVersion.

No platform controller directly updates Product/Settlement/Merchant tables.
Penalty executor issues domain commands.
