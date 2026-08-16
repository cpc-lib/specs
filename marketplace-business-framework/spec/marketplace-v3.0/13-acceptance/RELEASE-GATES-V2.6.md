# V2.6 Release Gates

Mandatory:
- MySQL/domain services remain search source of truth
- older/out-of-order search events cannot regress indexed version
- search index is rebuildable with alias-based cutover
- blocked/offline/prohibited offers cannot appear in buyer results
- deep pagination uses search_after/cursor policy
- Checkout/Trade revalidate price/inventory/saleability after Search
- behavior uses Kafka and contains no auth/payment secrets
- recommendation cannot mutate transaction/product state
- experiment assignment is stable and exposure is explicit
- review requires verified eligibility
- additional review/seller reply are append-only child facts
- review anti-abuse + moderation both enforced
- review summary is rebuildable
- favorite/follow and IM send are idempotent
- notification logical dedup/retry works
- support case uses domain commands, not foreign DB updates
- Buyer360 is read-only and cannot authorize money/order transitions
- privacy/retention policies cover behavior, search logs, IM, notification and recommendation
