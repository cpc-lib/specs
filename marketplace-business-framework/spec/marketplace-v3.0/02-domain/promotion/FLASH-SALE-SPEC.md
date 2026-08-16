# Flash Sale SPEC

Flow:
risk/qualification
→ campaign active check
→ per-user purchase limit
→ flash quota reserve
→ normal InventoryReservation
→ async/sync Trade create according to mode
→ coupon/promotion locks
→ Payment
→ flash quota commit
→ inventory commit.

Timeout/cancel:
release flash quota + normal inventory idempotently.

`FlashSaleReservation` is not authoritative warehouse stock.

Hot key measures:
- campaign/SKU sharded counters
- Redis Lua atomic reservation
- request token
- queue
- one user one request window
- device/risk limits
- asynchronous result query
