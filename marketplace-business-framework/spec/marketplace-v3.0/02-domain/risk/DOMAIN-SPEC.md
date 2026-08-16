# Risk Domain SPEC
Models:
- RiskRule
- RiskDecision
- RiskCase
- RiskSnapshot

Decision:
PASS / REVIEW / REJECT / CHALLENGE

Scenarios:
account takeover, coupon abuse, scalping, refund abuse, seller fraud, fake shipping, fake review, prohibited trade, cash-out.
Business services call RiskDecision but remain owners of business state.
