# V2.3 Fulfillment & AfterSale Test Matrix

## Forward Fulfillment
1. one order, two warehouses -> two FulfillmentOrders.
2. one OrderItem qty2 -> package1 qty1 + package2 qty1.
3. concurrent package creation cannot overship quantity.
4. duplicate ShipPackage idempotent.
5. carrier tracking duplicate and out-of-order event cannot regress state.
6. carrier DELIVERED alone does not complete MerchantOrder before receive policy.
7. rejection creates RETURNING path, not order delete/cancel.
8. digital item creates no physical Shipment.

## Reverse Fulfillment
9. RETURN_REFUND cannot refund before required inspection acceptance.
10. buyer return deadline expiration creates EXPIRED, refund not started.
11. partial inspection acceptance refunds only accepted quantity.
12. returned goods first enter quarantine; no automatic AVAILABLE increment.
13. RESTOCK_SELLABLE increments available exactly once.
14. exchange reserves replacement inventory; failure releases reservation.
15. replacement shipped cannot be compensated by deleting shipment.
16. repair SLA breach creates escalation.
17. active/completed aftersale quantity cannot exceed purchased quantity.
18. partial aftersale holds only affected settlement scope when configured.

## Chaos
- logistics provider timeout
- duplicate tracking callback
- reverse carrier unavailable
- inventory service unavailable during exchange
- payment provider UNKNOWN after return accepted
- MQ down after inspection commit: Outbox must recover
