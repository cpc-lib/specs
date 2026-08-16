# V2.4 Release Gates

Mandatory:
- published product/offer versions are immutable
- normalized SKU sale-attribute combination is unique
- category required attributes validated by schema version
- required brand authorization/compliance revalidated on publish
- pricing is server authoritative and deterministic
- historical orders never recalculate with current price/promotion rules
- price overlap/priority ambiguity is rejected or explicitly governed
- promotion compatibility result is deterministic
- funding party allocation equals actual benefit funding
- budget reserved + consumed never exceeds total
- quota reserved + consumed never exceeds total
- coupon claim/use limits concurrency-safe
- coupon instance cannot be used twice
- flash quota is not warehouse inventory
- gift/bundle inventory uses normal InventoryReservation
- promotion/flash Redis projections are reconcilable from durable facts
- SubmitTrade detects stale checkout pricing/promotion snapshots
