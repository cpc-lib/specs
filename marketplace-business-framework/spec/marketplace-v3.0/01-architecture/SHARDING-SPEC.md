# Sharding SPEC

## Principles
- Do not use one universal shard key.
- Avoid cross-shard transaction in hot write path.
- Preserve global business numbers for routing/lookups.
- Build reverse indexes/read models for alternate query dimensions.

## Suggested Routing
| Table Family | Shard Key | Notes |
|---|---|---|
| trade / merchant_order / order_item | buyer_id hash | buyer order path |
| merchant_order_index | merchant_id hash | seller read model |
| inventory_stock/reservation/ledger | sku_id hash or warehouse_id+sku_id | high concurrency |
| payment_order/attempt/transaction | payment_no hash | callback routing |
| refund | refund_no hash | provider callback |
| settlement | merchant_id hash | merchant finance |
| review | offer_id/product_id hash | product detail |
| cart | user_id hash | hot user data |
| coupon_wallet | user_id hash | claim/use |
| behavior | Kafka partition by user/session | analytics |

## IDs
Snowflake/segment IDs. Business numbers are globally unique and route-friendly.

## Growth Strategy
Small tables remain unsharded:
category, brand, rule config, provider config.

Migration uses:
expand → dual-read/controlled sync → verify → route switch → contract.
