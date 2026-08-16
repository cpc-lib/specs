# Commission & Settlement Eligibility SPEC

## CommissionRule
Dimensions:
- merchant
- category
- shop
- offer (exception only)
- campaign

Fields include effectiveFrom/effectiveTo/version/status.

## CommissionSnapshot
Settlement-relevant order fact stores:
- ruleId/version
- rate/fixedFee
- category/shop/merchant inputs
- calculated commission
- calculation trace

## SettlementEligibility
States:
PENDING / HOLDING / ELIGIBLE / CONSUMED / CANCELLED

Blocking reasons:
- WAITING_RECEIVE
- HOLD_PERIOD
- AFTERSALE_OPEN
- DISPUTE_OPEN
- RISK_FROZEN
- REFUND_IN_PROGRESS
- MANUAL_HOLD

Eligibility is locked/consumed when SettlementBatch selects it so one order amount cannot enter two batches.
