# Testcontainers Matrix

| Module | Containers | Mandatory Assertions |
|---|---|---|
| Trade | MySQL, Redis, RabbitMQ | idempotent submit, outbox, multi-order split |
| Inventory | MySQL, Redis | 1000 concurrent reserve, no negative stock |
| Promotion | MySQL, Redis | coupon lock/use/release, budget concurrency |
| Payment | MySQL, RabbitMQ | callback x100, UNKNOWN recovery, exact amount validation |
| AfterSale/Refund | MySQL, RabbitMQ | partial refund, quota concurrency |
| Settlement | MySQL, RabbitMQ | eligibility consumed once, commission snapshot, payout UNKNOWN |
| Search | MySQL, RabbitMQ, Elasticsearch/OpenSearch | stale event protection, rebuild |
| Reconciliation | MySQL | 0.01 mismatch, repair workflow |

No test may require a developer's locally pre-installed database.
