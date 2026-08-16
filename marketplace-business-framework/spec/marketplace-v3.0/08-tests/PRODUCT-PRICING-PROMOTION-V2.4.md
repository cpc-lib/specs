# V2.4 Product / Pricing / Promotion Test Matrix

## Product
1. required category attribute missing -> publish rejected.
2. two SKUs with same normalized SALE attributes -> duplicate rejected.
3. published version immutable; edit creates version N+1.
4. merchant without required active brand authorization cannot publish.
5. expired authorization blocks new publish but historical order snapshot remains valid.
6. compliance FAIL/EXPIRED follows blocking policy.
7. old ProductPublished event cannot overwrite newer Search document.

## Pricing
8. region/member/channel candidates use deterministic precedence.
9. same specificity collision rejected unless policy permits.
10. price change below floor triggers approval.
11. SubmitTrade recomputation detects stale checkout price.
12. same inputs/rule versions produce identical price snapshot/hash.

## Promotion
13. platform coupon + shop coupon stack when policy allows.
14. two mutually exclusive shop promotions select deterministic winner.
15. platform-funded ¥10 discount preserves merchant entitlement according to FundingAllocation.
16. merchant-funded ¥10 discount reduces merchant entitlement.
17. budget reservations cannot exceed campaign budget.
18. quota reservations cannot exceed total quota.
19. coupon instance cannot be used twice.
20. per-user coupon claim limit holds under concurrency.
21. purchase limit counts reserved + committed according to policy.
22. gift physical SKU reserves inventory.
23. bundle reserves component SKU inventory.
24. flash quota reservation does not imply warehouse inventory success.
25. flash cancel/timeout releases quota + inventory idempotently.
26. Redis promotion counter loss can rebuild/reconcile from durable reservations.

## Concurrency
- coupon total quota 100, 10,000 concurrent claims -> <=100 issued
- campaign budget ¥1000, concurrent reservations -> reserved+consumed<=1000
- flash SKU quota 100, 100,000 requests -> <=100 durable flash reservations
- per-user limit 1, 100 concurrent requests same user -> <=1 effective reservation
