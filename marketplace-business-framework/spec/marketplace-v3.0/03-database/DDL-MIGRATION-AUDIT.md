# DDL / Flyway Audit — V3.0

## Frozen checks
- migration version unique inside each service
- CREATE TABLE logical name unique inside repository baseline
- per-service ownership explicit
- outbox is template, not shared global schema
- hot binding-table child rows carry explicit route key
- money fields use DECIMAL
- finance/payment/refund/settlement facts are append/reversal oriented

## Fixed during V3.0 freeze
1. Settlement had two `V3` migrations. `merchant_funds_hold` is now `V4`.
2. Trade child allocation/economics tables were missing `buyer_id` route key.
3. Payment child/refund tables were missing `payment_no` route key.
4. Settlement item/payout child facts were missing `merchant_id` route key.
5. Review child records were missing `offer_id` route key.
6. IM message/read cursor lacked `buyer_id` binding route key.

See:
- `11-codegen/MIGRATION-REGISTRY.yaml`
- `11-codegen/SHARDING-ROUTING-FROZEN.yaml`

For an already-live large database, execute routing-key changes as expand → online backfill → validate → contract rather than one blocking migration.
