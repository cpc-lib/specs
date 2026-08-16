# Risk Engine Deepening SPEC

## Models
- RiskRuleSet
- RiskFeatureSnapshot
- RiskDecision
- RiskCase
- RiskActionRecommendation

## Scenarios
LOGIN
ACCOUNT_CHANGE
MERCHANT_ONBOARDING
PRODUCT_PUBLISH
COUPON_CLAIM
FLASH_SALE
CHECKOUT
TRADE_SUBMIT
PAYMENT
REFUND
AFTERSALE
REVIEW
SETTLEMENT
PAYOUT
WITHDRAWAL

## Decision
PASS
REVIEW
REJECT
CHALLENGE
HOLD

## Required Output
decision
riskScore
reasonCodes
ruleSetVersion
featureSnapshotId
challengeType(optional)
holdScope(optional)
expiresAt(optional)

Risk never directly performs:
inventory decrement
payment/refund
merchant suspension
settlement mutation
payout.

Business Application Service consumes decision and applies domain policy.
