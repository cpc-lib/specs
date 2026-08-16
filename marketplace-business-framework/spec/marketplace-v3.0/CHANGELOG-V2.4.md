# CHANGELOG V2.4

## Added
- category attribute schema/versioning
- SPU/SKU normalized specification model
- immutable SPU/SKU/Offer publishing versions
- brand authorization and product compliance snapshot
- regional/channel/member price books
- deterministic pricing pipeline
- promotion rule/scope/compatibility engine
- coupon template/claim/usage rule
- campaign budget/quota reservations
- per-user purchase limits
- gift and bundle semantics
- flash sale quota reservation integrated with normal inventory
- price/promotion calculation trace and snapshot contracts
- additional DDL/OpenAPI/events/state matrices/tests

## Compatibility
V2.4 is additive over V2.3. Existing Trade, Funding, Payment, Fulfillment,
AfterSale, Settlement and Reconciliation facts remain authoritative.
