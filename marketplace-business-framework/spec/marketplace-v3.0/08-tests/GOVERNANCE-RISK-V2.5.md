# V2.5 Merchant / Governance / Risk Test Matrix

## Merchant onboarding
1. enterprise applicant missing required verification -> cannot approve.
2. C2C applicant identity verified but risk REJECT -> application rejected.
3. approved merchant activation creates exactly one Merchant + admin membership.
4. duplicate approval command idempotent.
5. settlement profile change after ACTIVE requires high-risk workflow.
6. category admission expired -> new Offer publish rejected.

## Deposit
7. deposit transaction append-only.
8. deduction requires approved violation/penalty source.
9. concurrent deductions cannot make available balance negative.
10. refund UNKNOWN retains refund reservation.

## Violation / Penalty / Appeal
11. confirmed counterfeit violation can generate product block + funds hold.
12. repeated ExecutePenalty remains one effective action.
13. appeal FULL_REVERSE creates reversal action; original penalty stays historical.
14. platform governance service never directly updates foreign aggregate tables.

## Risk
15. same feature/rule snapshot produces deterministic decision.
16. Risk REJECT prevents TradeSubmit through Trade application policy.
17. Risk HOLD for settlement creates explicit MerchantFundsHold.
18. CHALLENGE does not equal APPROVED transaction.
19. raw secrets/payment credentials absent from risk feature snapshot.
20. duplicate abuse signal event idempotent by eventNo.

## Funds hold
21. active payout hold blocks Payout provider request.
22. order-item scoped hold does not freeze unrelated merchant order when policy supports partial scope.
23. hold release revalidates source case.
24. expired hold does not auto-release if reviewRequired policy says manual review.

## Merchant exit
25. open aftersale blocks exit.
26. Payout UNKNOWN blocks exit.
27. negative balance blocks deposit refund/exit close.
28. callbacks/refunds continue while merchant business is frozen.
29. exit close retains historical orders/settlements for authorized audit.
