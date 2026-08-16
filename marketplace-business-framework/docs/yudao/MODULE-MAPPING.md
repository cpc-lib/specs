# yudao-cloud → Marketplace Business Framework

| yudao concept/module | Decision | Marketplace target |
|---|---|---|
| dependencies/BOM | REFERENCE | marketplace-dependencies |
| framework common/web/security/mybatis/redis | REFERENCE + REWRITE | marketplace-framework/* |
| gateway | REFERENCE + REWRITE | marketplace-gateway |
| system | REFERENCE + REWRITE | marketplace-system |
| infra file | EXTRACT MINIMUM | marketplace-file |
| bpm/Flowable | DEFER | add when merchant/product/price approval task needs it |
| pay | ADAPTER REFERENCE ONLY | marketplace-payment provider adapters later |
| member | REWRITE | marketplace-user + merchant membership |
| im | BUSINESS REFERENCE | marketplace-customer-service later |
| wms | DEFER | supply-chain extension later |
| mall | DO NOT ADOPT DOMAIN MODEL | V3.0 Marketplace modules are authoritative |

No yudao source module is a runtime Maven dependency of this framework.
