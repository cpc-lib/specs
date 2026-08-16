# High Concurrency SPEC

## Hot Paths
- product detail
- search
- cart
- checkout
- inventory reservation
- order submit
- payment callback
- flash sale

## Techniques
- cache preheat
- local cache + Redis
- hot-key splitting
- single-flight
- gateway token bucket
- per-user/device/SKU limits
- Lua atomic front reservation for flash sale
- MQ buffering
- DB conditional update
- idempotency key
- async read-model writes
- downgrade non-core features

## Non-Degradable Correctness
Never degrade:
- payment correctness
- refund quota
- inventory correctness
- order uniqueness
- settlement accounting

## Flash Sale
Gateway
→ Risk
→ Qualification
→ Rate Limit
→ Redis Lua
→ Queue
→ asynchronous Trade create
→ DB inventory ledger
→ compensation/reconciliation
