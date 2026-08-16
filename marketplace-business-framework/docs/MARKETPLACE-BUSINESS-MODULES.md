# Marketplace Business Modules — V1.1 Complete

V1.1 exposes all **26 Marketplace business modules directly at repository root**.

| # | Module | Responsibility |
|---:|---|---|
| 01 | `marketplace-user` | Buyer account/profile/address and buyer-side identity context. |
| 02 | `marketplace-merchant` | Merchant onboarding, verification, qualification, deposit, credit and exit. |
| 03 | `marketplace-shop` | Shop lifecycle, merchant membership and shop-scoped authorization. |
| 04 | `marketplace-product` | Platform category, attributes, brand, SPU, SKU, Offer and compliance. |
| 05 | `marketplace-pricing` | Server-authoritative price books, price rules and immutable pricing snapshots. |
| 06 | `marketplace-inventory` | Warehouse SKU stock, reservations and immutable inventory ledger. |
| 07 | `marketplace-promotion` | Campaigns, coupons, promotion budgets/quotas, gifts, bundles and flash sales. |
| 08 | `marketplace-cart` | Buyer cart grouped by merchant/shop with no price freezing. |
| 09 | `marketplace-checkout` | Checkout session, quote and authoritative pre-trade validation. |
| 10 | `marketplace-trade` | Cross-shop Trade, MerchantOrder, OrderItem and immutable transaction snapshots. |
| 11 | `marketplace-payment` | Payment order/attempt/transaction, provider adapters and refund money facts. |
| 12 | `marketplace-fulfillment` | Fulfillment planning, package/shipment, routing, delivery confirmation and digital fulfillment. |
| 13 | `marketplace-aftersale` | After-sale case, reverse logistics, inspection, exchange, repair and timeout policy. |
| 14 | `marketplace-dispute` | Dispute evidence, arbitration decision and domain-command execution. |
| 15 | `marketplace-settlement` | Merchant settlement eligibility, payable, balances, holds, payouts and adjustments. |
| 16 | `marketplace-finance` | Payment clearing, double-entry finance ledger, accounting posting and daily close. |
| 17 | `marketplace-reconciliation` | Provider statement, business matching, exception handling and financial reconciliation. |
| 18 | `marketplace-invoice` | Invoice application, issue, red flush, reissue and delivery. |
| 19 | `marketplace-review` | Verified purchase reviews, additional reviews, seller replies and moderation. |
| 20 | `marketplace-risk` | Scenario risk rules, feature snapshots, decisions, cases and anti-abuse signals. |
| 21 | `marketplace-governance` | Violation, penalty, appeal, prohibited goods and IP/counterfeit governance. |
| 22 | `marketplace-search` | Derived search projections, query understanding, ranking and reindexing. |
| 23 | `marketplace-recommendation` | Recommendation candidates, model/policy versions and experiments. |
| 24 | `marketplace-notification` | Notification preferences, templates, messages and delivery facts. |
| 25 | `marketplace-customer-service` | Customer service cases and buyer-seller communication context. |
| 26 | `marketplace-cqrs` | CQRS read models such as Buyer360 and denormalized marketplace views. |

`marketplace-system` and `marketplace-gateway` are platform modules and are not counted in the 26 business modules.

## Why V1.1 changed
V1.0 nested all business modules under `marketplace-services/`. Although Maven-valid,
that made the repository look incomplete and most modules were only thin placeholders.

V1.1 removes that aggregator and gives every business module:
- its own top-level directory,
- its own `pom.xml`,
- a Spring Boot application entry,
- `application.yml`,
- `db/migration/`,
- complete DDD layer directories,
- domain slices mapped to Marketplace V3.0,
- `MODULE-SPEC.md`.
