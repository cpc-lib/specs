# Marketplace SPEC V2.2 — START HERE

定位：自营 + B2C + C2C、超大规模 Spring Cloud Marketplace。

V2.2 是 Contract Hardening 版本，不扩新业务领域。

阅读顺序：
1. `00-master/MASTER-SPEC-V3.0.md`
2. `01-architecture/BOUNDED-CONTEXT.md`
3. `11-codegen/COMMAND-QUERY-CATALOG.yaml`
4. `11-codegen/STATE-TRANSITION-MATRIX.yaml`
5. `11-codegen/SAGA-COMPENSATION-MATRIX.yaml`
6. `11-codegen/MONEY-CALCULATION-SPEC.md`
7. 对应 TASK 的 `14-task-bundles/TASK-xxx/CONTEXT.md`

核心实现红线：
- Merchant != Shop
- SPU != SKU != Offer
- Trade != MerchantOrder != OrderItem
- client price/merchantId/status never authoritative
- PaymentTransaction is money fact; settlement is separate
- no hidden 0.01 rounding loss
- no concurrent over-refund
- no normal-mode Redis-only stock truth
- no broadcast seller order scans across buyer shards

## V2.2 Finance Reading
Read FUNDING-RESPONSIBILITY-SPEC, PAYMENT-CLEARING-SPEC, REFUND-REVERSE-CLEARING-SPEC, MERCHANT-FUNDS-SETTLEMENT-SPEC and ACCOUNTING-POSTING-MATRIX before Payment/Settlement codegen.

## V2.3 Focus
Forward fulfillment, partial shipment, warehouse routing, digital fulfillment,
reverse logistics, return inspection, exchange, repair and settlement interaction.

## V2.4 Focus
Catalog attribute schema, immutable product versions, brand authorization,
deterministic pricing, promotion compatibility, coupons, budgets/quotas, gifts,
bundles and flash-sale consistency.

## V2.5 Focus
Merchant onboarding, enterprise/C2C verification, category admission, merchant deposit,
credit/level, violations, penalties, appeals, prohibited goods/IP governance, risk decisions,
funds holds and merchant exit/closure.

## V2.6 Focus
Search/OpenSearch projections, query understanding/ranking, Kafka behavior events,
recommendation/experiments, review social proof, favorite/follow, notification,
customer service, IM and Buyer 360 read models.

## V3.0 — FROZEN CODEGEN BASELINE
Do not start from old version changelogs. Start with:
1. `00-master/MASTER-SPEC-V3.0.md`
2. `00-master/V3.0-FROZEN-CONTRACT.md`
3. `00-master/CODEGEN-ENTRYPOINT.md`
4. `VALIDATION-REPORT.md`
5. the selected TASK bundle.
