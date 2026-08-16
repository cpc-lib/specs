# Test Plan

## Mandatory
- domain unit tests
- repository tests
- Testcontainers integration
- OpenAPI contract tests
- event schema tests
- tenant/merchant isolation tests
- concurrency tests
- E2E
- chaos
- performance
- security

## Critical scenarios
1. 1000 concurrency, stock=100 -> <=100 successful reservations.
2. same CheckoutToken x100 -> exactly one Trade.
3. multi-merchant cart -> 1 Trade + N MerchantOrders.
4. duplicate payment callback x100 -> 1 money effect.
5. refundable=100, concurrent refund 80+80 -> total <=100.
6. settlement 100 gross -5 commission -> 95 payable.
7. refund after settlement -> negative balance / next settlement adjustment.
8. Merchant A cannot query Merchant B order.
9. Redis down normal trade still cannot oversell.
10. MQ down local business commit with outbox; recover later.
11. ES down does not block trade/payment/refund.
12. provider UNKNOWN converges by query.
