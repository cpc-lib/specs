# Marketplace MASTER SPEC V3.0 — CODEGEN READY

## 1. Platform Positioning
企业级超大规模 Marketplace，覆盖：
- 平台自营
- B2C 企业商户
- C2C 个人卖家

## 2. Core Transaction Chain
User
→ Merchant/Seller
→ Shop
→ SPU/SKU/Offer
→ Pricing
→ Inventory
→ Cart
→ CheckoutSession
→ Trade
→ MerchantOrder
→ OrderItem
→ Payment
→ Fulfillment
→ Shipment
→ Receive
→ AfterSale/Refund/Dispute
→ Settlement
→ MerchantPayable
→ Payout
→ Reconciliation

## 3. Core Bounded Contexts
- Identity/User
- Merchant
- Shop
- Catalog
- Product
- Pricing
- Inventory
- Cart
- Promotion/Coupon
- Checkout
- Trade
- Payment
- Fulfillment/Logistics
- AfterSale
- Dispute
- Review
- Settlement
- Finance
- Invoice
- Reconciliation
- Search
- Recommendation
- Risk
- Moderation
- Notification
- Customer Service
- Procurement/WMS Integration

## 4. Business Model Rules
### Merchant / Shop
Merchant 是经营主体；Shop 是经营门店。一个 Merchant 可以拥有多个 Shop。

### SPU / SKU / Offer
- SPU：标准商品
- SKU：规格商品
- Offer：某商户/店铺对某 SKU 的实际售卖实体

### Trade / MerchantOrder / OrderItem
- Trade：买家一次提交
- MerchantOrder：按 Merchant/Shop 拆单
- OrderItem：交易事实最小粒度

### Payment
PaymentOrder 表示业务支付意图；
PaymentAttempt 表示一次渠道支付尝试；
PaymentTransaction 表示渠道确认的资金事实。

### Settlement
买家支付成功后，资金进入待清分/待结算，不直接成为商户可提现余额。

## 5. Money Rules
- Java BigDecimal
- MySQL DECIMAL(18,2)
- payment gateway may use minor units (fen/cents)
- no float/double
- all allocation residual uses RoundingResidualPolicy

## 6. Inventory Rules
Inventory source of truth:
- inventory_stock
- inventory_reservation
- inventory_ledger

Normal flow:
AVAILABLE → RESERVED → COMMITTED
or RESERVED → RELEASED/EXPIRED

Flash sale may use Redis front reservation, but DB ledger/reconciliation remains required.

## 7. Order Create Saga
Checkout valid
→ create Trade + MerchantOrders + OrderItems
→ reserve inventory
→ lock coupons
→ reserve promotion quota
→ create payment relation
→ WAITING_PAYMENT

Any partial failure must compensate completed external steps.

## 8. Payment Rules
- callback validates signature/merchant/app/paymentNo/currency/amount/status
- duplicate callback safe
- UNKNOWN never treated as FAILED
- client payment success is not authoritative
- query-before-retry for unknown provider result

## 9. Refund Rules
Refundable:
successful paid allocation
- successful refunds
- reserved refunds
- non-refundable allocations

Concurrent refund must not exceed refundable quota.

## 10. Settlement Rules
Eligibility normally requires:
- paid order
- completed/received
- settlement hold period elapsed
- no blocking aftersale/dispute
- no risk freeze

Settlement items freeze:
- merchant gross
- platform commission
- payment fee
- merchant promotion
- platform subsidy
- refund/compensation adjustment
- tax-related amount
- final payable

## 11. C2C
Must include:
- identity verification
- prohibited goods
- risk review
- seller credit
- delayed settlement
- dispute arbitration
- evidence retention
- settlement freeze

## 12. Multi-Merchant Isolation
Seller/admin API derives merchant/shop scope from authenticated membership.
Client merchantId/shopId is never trusted as authority.

## 13. Events
Business TX + Outbox.
Consumer TX + Inbox.
Kafka for clickstream/behavior/BI.
RabbitMQ for transactional events.

## 14. Search
MySQL is truth.
Outbox → MQ → Search read model.
Search outage must not block order/payment/refund.

## 15. Sharding
Different context uses different key:
- trade: buyer_id
- payment: payment_no hash
- inventory: sku_id or warehouse_id+sku_id
- settlement: merchant_id
- review: product/offer key
- behavior: Kafka partitions

## 16. High Concurrency
Core protections:
- gateway rate limit
- risk/challenge
- hot-key isolation
- Redis/Lua where appropriate
- async queue
- DB conditional updates
- idempotency
- degradation
- circuit breaker

## 17. API Boundaries
- /api/buyer/v1
- /api/seller/v1
- /api/platform/v1
- /internal/v1

State transitions use command endpoints, never arbitrary status updates.

## 18. Codegen Policy
AI must read MASTER + domain + DDL + API + event + tests + task context.
If contract is missing: report `SPEC-GAP`; do not invent destructive domain changes.


# 19. V2.1 Contract Hardening
V2.1 does not add a new marketplace business domain. It hardens implementation contracts.

## 19.1 Order Amount Conservation
For one Trade:
- sum(MerchantOrder payable) = Trade payable
- sum(OrderItem allocated discount by funding party) = corresponding Trade discount
- all rounding residual must be explicitly assigned by RoundingResidualPolicy
- historical allocation is immutable after Trade creation; after-sale creates adjustment facts.

## 19.2 Trade Payment Allocation
A successful PaymentTransaction is allocated to Trade/MerchantOrder by `trade_payment_allocation`.
Payment success does not directly mutate MerchantBalanceAccount.
Settlement eligibility consumes completed transaction/order facts after applicable hold/risk rules.

## 19.3 Discount Funding
Every discount amount identifies funding party:
- PLATFORM
- MERCHANT
- SHOP
- BRAND (future/optional)
- OTHER_APPROVED
Refund and settlement use the original `discount_allocation` snapshot.

## 19.4 Refund Quota
Refund quota is computed from immutable paid/order allocation facts. Provider refund is called only after quota reservation commits.
UNKNOWN refund keeps the reservation until authoritative provider query resolves.

## 19.5 Commission Snapshot
Commission rules are effective-dated and snapshotted at the settlement-relevant transaction fact. Rule changes never recompute historical completed settlement items.

## 19.6 Settlement Eligibility
Eligibility is a first-class record, not a boolean derived ad hoc at payout time. It records completion time, hold-until, blocking aftersale/dispute/risk reason and eligibility version.

## 19.7 Codegen Contracts
V2.1 adds:
- complete command/query catalog
- state transition matrix
- Saga compensation matrix
- money calculation contract
- request/response schemas for core OpenAPI
- detailed finance/discount/payment/settlement DDL
- ShardingSphere sample routing
- Testcontainers contract matrix
- release gates

# 20. V2.2 Transaction & Finance Deepening

V2.2 freezes the marketplace money chain from order economics to channel payment, clearing, refund reversal, merchant settlement, payout and reconciliation.

## 20.1 Regulatory Boundary
`GUARANTEED_TRANSACTION` means the marketplace controls business fulfillment and delays settlement according to policy. It does NOT mean the marketplace may hold customer money without the required legal/payment permissions.
Actual fund custody, split payment and payout are executed through licensed PSP/bank capabilities appropriate to the deployment jurisdiction.
The platform system owns business/payment/clearing/settlement facts and reconciliation, not regulatory authorization.

## 20.2 Economic Amount Chain
For each OrderItem the system freezes:
- merchandiseGross
- allocatedShipping
- allocatedTax
- platformFundedDiscount
- merchantFundedDiscount
- shopFundedDiscount
- other funded discount
- buyerCashAllocation
- merchantGrossEntitlement
- commissionBase
- settlementBase

All refund and settlement calculations consume these immutable snapshots.

## 20.3 Funding Responsibility
Every promotion/discount has a funding party. Buyer cash reduction and merchant economic loss are not assumed to be the same thing.
A platform-funded coupon reduces buyer payable while normally preserving merchant entitlement according to the campaign contract.
A merchant-funded coupon reduces merchant entitlement.

## 20.4 Payment Clearing
PaymentTransaction is a channel money fact.
PaymentClearingRecord and PaymentClearingAllocation represent operational money clearing into trade/order economic buckets.
Payment success must never directly write merchant AVAILABLE balance.

## 20.5 Refund Reverse Clearing
Refund is calculated as a reversal of original funding/allocation facts:
- buyer cash refund
- platform subsidy reversal
- merchant funded discount reversal/economic adjustment
- shipping/tax reversal according to policy
- merchant pending/settled liability adjustment
- ledger reversal

## 20.6 Merchant Funds Lifecycle
Merchant funds move through explicit buckets:
PENDING -> AVAILABLE/ELIGIBLE -> SETTLING -> PAID
and may be FROZEN or NEGATIVE.
Every change has append-only ledger fact.

## 20.7 Settlement Hold
C2C/high-risk merchants can have effective-dated hold policies and per-order hold records. Risk/dispute/aftersale can extend or freeze settlement eligibility.

## 20.8 Merchant Payable & Payout
Approved settlement creates MerchantPayable. Payout first reserves payable amount, then invokes PSP/bank.
Payout UNKNOWN retains reservation and requires provider query.
Merchant-requested withdrawal is optional and only enabled when the configured PSP/bank product legally supports it.

## 20.9 Finance Ledger
Operational subledger and accounting posting are separated. Statutory posting policy can vary by merchant model/jurisdiction, but every posting entry must balance and reference immutable source facts.

## 20.10 Daily Close & Reconciliation
Daily control checks compare:
Provider statement -> PaymentTransaction -> PaymentClearing -> TradeAllocation -> MerchantPending -> Settlement -> MerchantPayable -> Payout -> Ledger.
Any unexplained 0.01 difference is an exception.

# 21. V2.3 Fulfillment & AfterSale Deepening

V2.3 freezes forward and reverse fulfillment semantics.

## 21.1 Three-Layer Fulfillment Model
- `FulfillmentOrder`: fulfillment plan and warehouse responsibility.
- `Package`: physical/logical grouping of order-item quantities.
- `Shipment`: one carrier transportation fact for one package or shipment leg.

MerchantOrder state is a transaction summary and must not be used as the only shipment state.

## 21.2 Partial Fulfillment
A MerchantOrder may have:
- multiple FulfillmentOrders
- multiple warehouses
- multiple Packages
- multiple Shipments
- mixed shipped/unshipped item quantities

Item quantity conservation must hold:
orderedQty = unfulfilledQty + allocatedToPackageQty + cancelledQty + aftersaleAdjustedQty,
according to the current business phase.

## 21.3 Warehouse Routing
Routing inputs include:
delivery address/region, warehouse inventory, merchant fulfillment mode,
carrier capability, promised SLA, cold-chain/hazard constraints, cost and split-package penalty.
The selected route is snapshotted on fulfillment creation.

## 21.4 Delivery Evidence
Delivery status is based on carrier/provider facts plus platform policy.
Buyer-confirmed receive and system auto-receive are distinct facts.
Carrier `DELIVERED` does not automatically mean the marketplace MerchantOrder is completed.

## 21.5 Rejection
Buyer refusal/rejection creates a delivery exception / reverse logistics path.
Do not blindly mark the order CANCELLED after shipment.

## 21.6 Virtual/Digital Goods
Virtual goods do not create physical Shipment.
They use `DigitalFulfillment` / entitlement-delivery facts.
Digital delivery success and consumption policy determine completion/refund eligibility.

## 21.7 AfterSale Separation
AfterSaleCase is the customer-service/business decision aggregate.
Reverse physical fulfillment uses:
- ReturnOrder
- ReverseShipment
- ReturnInspection
- StockDisposition

Money movement remains Payment/Refund domain.

## 21.8 Return Refund
RETURN_REFUND:
AfterSale approved
→ ReturnOrder
→ buyer sends / pickup
→ ReverseShipment
→ warehouse/seller receives
→ ReturnInspection
→ refundable decision
→ RefundQuotaReservation
→ RefundOrder
→ RefundTransaction
→ reverse clearing / settlement adjustment.

## 21.9 Refund Only
REFUND_ONLY may skip reverse logistics only when policy permits, e.g.:
- not yet shipped
- low-value no-return policy
- digital/service specific policy
- platform arbitration decision

## 21.10 Exchange
Exchange is not modeled as "refund + new unrelated order".
Create ExchangeOrder linked to original AfterSaleCase:
return/inspection as required
→ reserve replacement inventory
→ replacement FulfillmentOrder/Package/Shipment
→ completed exchange.
All quantities and replacement SKU differences remain traceable.

## 21.11 Repair
RepairOrder tracks:
receive product
→ inspection
→ repair
→ quality check
→ return shipment
→ customer receive
→ close.
Repair does not mutate original order item.

## 21.12 Return Stock Disposition
Returned goods must not automatically increase AVAILABLE stock.
Inspection decision:
- RESTOCK_SELLABLE
- RESTOCK_DEFECTIVE
- REPAIR_REQUIRED
- RETURN_TO_VENDOR
- SCRAP
- QUARANTINE
Only RESTOCK_SELLABLE can increase normal available inventory.

## 21.13 Timeouts
Timeout clocks are explicit records/policies:
- seller review timeout
- buyer return timeout
- seller receive timeout
- inspection timeout
- exchange shipment timeout
- repair SLA
A timeout command must be idempotent and revalidate current state.

## 21.14 Dispute Evidence
Evidence is append-only metadata referencing secure files/messages/logistics facts.
Decision uses an immutable evidence snapshot/version and produces auditable execution commands.

## 21.15 Settlement Interaction
Any blocking aftersale/dispute/return/refund can HOLD settlement eligibility for affected economic scope.
Partial aftersale must only freeze the affected item/economic amount when platform policy supports item-level settlement.

# 22. V2.4 Product / Pricing / Promotion Deepening

V2.4 freezes catalog, product publishing, price calculation and promotion composition semantics.

## 22.1 Product Model
- PlatformCategory: platform standard taxonomy.
- CategoryAttributeDefinition: attribute schema by category/version.
- SPU: standard product.
- SKU: concrete specification combination.
- Offer: one merchant/shop selling one SKU.
- Product/Offer versions are immutable after publication; changes create new versions.

## 22.2 Attribute Types
Category attributes:
- KEY: product-identifying attributes.
- SALE: SKU dimension attributes.
- NORMAL: descriptive attributes.
- SEARCH: search/filter fields.
- COMPLIANCE: qualification/regulatory fields.

Attributes define data type, requiredness, option source, validation and searchability.

## 22.3 SKU Combination
SKU combinations must be unique inside an SPU for the normalized sale-attribute set.
Changing sale dimensions after transactions exist requires new SKU/version, not destructive mutation.

## 22.4 Brand Authorization
Third-party merchants may need category/brand authorization.
Authorization is effective-dated and supports:
PENDING / ACTIVE / EXPIRED / REVOKED / REJECTED.
Publishing an Offer revalidates current authorization where policy requires it.
Order snapshot preserves the authorization/compliance fact version used at sale time.

## 22.5 Product Publishing
DRAFT
→ VALIDATING
→ PENDING_REVIEW
→ APPROVED
→ ONLINE
→ OFFLINE

ONLINE may enter BLOCKED by moderation/governance.
Published content creates immutable ProductVersion/OfferVersion snapshots.

## 22.6 Pricing Sources
A price result may depend on:
- base/list price
- price book
- region
- channel
- member level
- merchant/shop scope
- effective date/time
- direct promotion price
- flash-sale price

Client-submitted price is never authoritative.

## 22.7 Deterministic Price Pipeline
Pricing/Promotion computation order:
1. validate Offer/SKU/merchant/shop saleability
2. resolve base/list price
3. resolve region/channel/member price candidates
4. select deterministic effective price
5. evaluate item-level promotion candidates
6. evaluate order/shop/platform promotion candidates
7. build compatibility/exclusion graph
8. select valid benefit plan
9. apply coupons
10. apply shipping benefits
11. calculate tax according to frozen tax policy
12. allocate funding responsibilities
13. allocate cross-item discounts
14. apply deterministic residual rounding
15. persist PricingSnapshot + PromotionSnapshot + FundingAllocation.

For identical inputs and rule versions, result must be identical.

## 22.8 Promotion Types
- DIRECT_DISCOUNT
- PERCENTAGE_DISCOUNT
- FULL_REDUCTION
- FULL_DISCOUNT
- N_FOR_FIXED_PRICE
- N_ITEMS_DISCOUNT
- BUY_X_GET_Y
- GIFT
- BUNDLE
- MEMBER_PRICE
- FLASH_SALE
- FREE_SHIPPING

## 22.9 Promotion Ownership
Owner:
PLATFORM / MERCHANT / SHOP / BRAND / PARTNER.

Funding responsibility is independent from display ownership and uses V2.2 FundingParty semantics.

## 22.10 Compatibility
PromotionCompatibilityPolicy determines:
- STACKABLE
- MUTUALLY_EXCLUSIVE
- PRIORITY_EXCLUSIVE
- BEST_BENEFIT_ONLY
- REQUIRED_COMBINATION

A deterministic tie-break is mandatory.

## 22.11 Coupon
CouponTemplate defines issuance/usage policy.
CouponWallet is user-held coupon instance.
Lifecycle:
AVAILABLE -> LOCKED -> USED
AVAILABLE -> EXPIRED
LOCKED -> RELEASED / USED

Claim and usage limits are separate.
Coupon lock is part of CreateTrade Saga.

## 22.12 Quotas & Limits
Supported:
- total campaign quota
- SKU activity quota
- per-user claim limit
- per-user purchase limit
- per-device/risk limit
- per-order limit
- regional/channel limit

Hot-path quota can use Redis/Lua but requires durable reservation/commit/release and reconciliation.

## 22.13 Flash Sale
Flash sale is a promotion + eligibility + hot quota layer.
It does not replace authoritative InventoryReservation.
Flow:
qualification/risk
→ flash quota reservation
→ normal inventory reservation
→ Trade create
→ payment
→ quota commit.
On failure/timeout: release idempotently.

## 22.14 Gift
A gift is a zero-buyer-price economic line with explicit funding/cost responsibility.
Gift inventory must be reserved like normal inventory if physical.

## 22.15 Bundle
BundleOffer references component SKUs/quantities and bundle pricing policy.
Inventory is reserved on component SKUs; bundle itself does not invent physical stock unless WMS explicitly models kit stock.

## 22.16 Snapshot Rule
Refund, aftersale, settlement and reconciliation consume the transaction-time snapshot.
Never recalculate an old order using a newly modified promotion/price rule.

## 22.17 Search Consistency
Product/Offer online facts are MySQL truth.
Outbox events update Search document.
Older product/offer versions cannot overwrite newer indexed versions.

# 23. V2.5 Merchant / Platform Governance / Risk Deepening

V2.5 freezes merchant lifecycle, platform governance, risk decisions, violations, appeals and merchant exit semantics.

## 23.1 Merchant Identity
Merchant types:
- PLATFORM_SELF
- ENTERPRISE
- INDIVIDUAL

Enterprise merchant uses KYB-like business verification.
Individual C2C seller uses identity verification plus platform risk controls.

Verification facts are versioned and auditable:
- identity
- legal/business entity
- beneficial owner / representative where applicable
- settlement account
- tax identity
- address/contact
- category licenses
- brand authorization
- risk/compliance checks

The platform stores only data needed for its business/compliance model and applies encryption/masking/retention controls.

## 23.2 Merchant Application
Application lifecycle:
DRAFT
→ SUBMITTED
→ IDENTITY_REVIEW
→ QUALIFICATION_REVIEW
→ RISK_REVIEW
→ AGREEMENT_PENDING
→ DEPOSIT_PENDING(optional)
→ APPROVED
→ ACTIVATING
→ ACTIVE

Alternative:
REJECTED / WITHDRAWN / EXPIRED.

Flowable may orchestrate human approval, but business application state is owned by Merchant service.

## 23.3 Shop Admission
Creating a Shop is not sufficient for sale.
Shop/category admission may require:
- active merchant
- valid settlement profile
- merchant deposit
- category qualification
- brand authorization
- risk level
- shop type requirements
- platform policy

## 23.4 Merchant Deposit
MerchantDepositAccount is a dedicated liability/security subledger.
Lifecycle:
REQUIRED
→ PARTIALLY_PAID
→ PAID
→ FROZEN / PARTIALLY_DEDUCTED
→ REFUNDING
→ REFUNDED / CLOSED.

Deposit deduction must reference a Violation/Penalty/Settlement fact.
Direct balance overwrite is forbidden.

## 23.5 Merchant Credit & Level
MerchantCreditProfile aggregates operational metrics, not raw mutable score only:
- fulfillment performance
- cancellation rate
- aftersale/refund rate
- dispute rate
- violation severity
- counterfeit complaints
- customer service SLA
- logistics SLA
- payment/settlement exceptions
- verified review quality

Credit rules are versioned.
MerchantLevel is a platform policy output and may affect:
traffic eligibility, settlement hold, campaign eligibility, quota, deposit requirement, review frequency.

## 23.6 Violation
ViolationCase records:
- violationType
- subject
- rule/policy version
- evidence
- severity
- risk impact
- proposed penalties
- review/decision
- execution result.

Violation examples:
COUNTERFEIT
PROHIBITED_GOODS
MISLEADING_DESCRIPTION
PRICE_FRAUD
FAKE_SHIPMENT
BRUSH_ORDER
BRUSH_REVIEW
AFTERSALE_ABUSE
TRADE_MANIPULATION
OFF_PLATFORM_DIVERSION
IP_INFRINGEMENT
SERVICE_SLA_BREACH
COMPLIANCE_EXPIRED.

## 23.7 Penalties
PenaltyAction types:
WARNING
PRODUCT_TAKE_DOWN
PRODUCT_BLOCK
SHOP_LIMIT
SHOP_SUSPEND
MERCHANT_SUSPEND
DEPOSIT_FREEZE
DEPOSIT_DEDUCTION
FUNDS_HOLD
SETTLEMENT_DELAY
CAMPAIGN_RESTRICTION
TRAFFIC_RESTRICTION
ACCOUNT_TERMINATION.

Penalty execution is idempotent, auditable and reversible only through an approved reversal/appeal decision.

## 23.8 Appeal
Merchant can submit AppealCase against eligible violation/penalty.
Appeal does not silently revert the original decision.
Flow:
SUBMITTED
→ EVIDENCE_REVIEW
→ PLATFORM_REVIEW
→ DECIDED
→ EXECUTING
→ CLOSED.

Decision:
UPHOLD
PARTIAL_REVERSE
FULL_REVERSE
REOPEN_INVESTIGATION.

## 23.9 Prohibited/Restricted Goods
`ProductGovernancePolicy` defines:
- PROHIBITED
- RESTRICTED
- LICENSE_REQUIRED
- AGE_RESTRICTED
- REGION_RESTRICTED
- ALLOWED.

Publishing, checkout and post-sale governance may all revalidate as required.
Historical orders remain immutable.

## 23.10 Counterfeit / IP Governance
The platform supports:
- brand/IP complaint
- evidence submission
- merchant response
- authenticity review
- product takedown/block
- merchant risk escalation
- repeat-offender policy.

## 23.11 Risk Architecture
Risk service returns a decision, not arbitrary business status mutation:
PASS / REVIEW / REJECT / CHALLENGE / HOLD.

Scenarios include:
- LOGIN
- ACCOUNT_CHANGE
- MERCHANT_ONBOARDING
- PRODUCT_PUBLISH
- COUPON_CLAIM
- FLASH_SALE
- CHECKOUT
- TRADE_SUBMIT
- PAYMENT
- REFUND
- AFTERSALE
- REVIEW
- SETTLEMENT
- PAYOUT
- WITHDRAWAL.

Business service remains owner of its aggregate transition.

## 23.12 Device & Behavior Signals
Signals may include:
device fingerprint/reference, session risk, IP reputation, velocity, account relationships, payment relationships,
behavior pattern, historical abuse, merchant/customer graph signals.

Risk service should consume privacy-controlled derived features; raw secrets/credentials never become risk event payloads.

## 23.13 Anti-Abuse
Supported controls:
- coupon abuse
- flash-sale botting
- multi-account farming
- fake orders / brush orders
- fake reviews
- refund abuse
- seller-buyer collusion
- abnormal payment/refund graph
- account takeover
- payout/settlement anomaly.

## 23.14 Funds Hold
Risk/Governance may create `MerchantFundsHold` against:
- settlement eligibility
- merchant payable
- payout
- merchant available balance.

Hold scopes:
ALL_FUNDS
SETTLEMENT_BATCH
MERCHANT_ORDER
ORDER_ITEM
AMOUNT.

A hold is an explicit fact with reason, source case and expiry/review policy.
No hidden `if risk then don't pay` logic.

## 23.15 Merchant Suspension
SUSPENDED blocks new business according to policy:
- no new Offer publication
- no new sale/checkout eligibility
- optional existing fulfillment still allowed
- refund/aftersale/provider callbacks must continue
- reconciliation and payout may be held, not silently dropped.

## 23.16 Merchant Exit
MerchantExitCase:
REQUESTED
→ BUSINESS_FREEZE
→ INVENTORY_PRODUCT_OFFBOARD
→ OPEN_ORDER_SETTLEMENT
→ AFTERSALE_WINDOW
→ FINANCIAL_RECONCILIATION
→ DEPOSIT_SETTLEMENT
→ DATA_EXPORT_RETENTION
→ CLOSED.

Exit cannot close while:
open orders,
blocking aftersales/disputes,
refund/payout UNKNOWN,
unsettled merchant payable/negative balance,
critical reconciliation exceptions,
active legal/governance hold
remain unresolved unless an explicit platform close/write-off policy exists.

## 23.17 Platform Governance Audit
All high-risk platform actions require:
operator
reason
policy/rule version
evidence
before/after facts
trace/correlation
optional dual approval
immutable audit.

# 24. V2.6 Search / Recommendation / Review / Customer Experience Deepening

V2.6 freezes read-model, discovery, social proof and customer-experience semantics.

## 24.1 Search Principle
MySQL transactional databases remain source of truth.
Search uses Elasticsearch/OpenSearch derived read models.

Flow:
Product/Offer/Price/Inventory/Promotion/Review/Shop facts
→ Outbox/RabbitMQ
→ Search Projection
→ Elasticsearch/OpenSearch.

Search index is rebuildable and may be eventually consistent.
Search outage must not block payment/refund/settlement.

## 24.2 Search Document
The primary offer document may include:
offerId, offerVersionId, dataVersion,
spuId/skuId/category/brand,
merchantId/shopId,
title/tokens/searchable attributes,
effective/display price,
promotion labels,
saleability,
regional availability,
sales/rating summaries,
shop quality,
risk/governance flags,
ranking features,
updatedAt.

No secret/PII fields are indexed.

## 24.3 Projection Ordering
Projection events carry source version.
Document update uses compare-and-set/script logic:
incomingVersion >= indexedVersion.
Old/out-of-order events cannot overwrite newer read model state.

## 24.4 Search Query
Supported:
keyword search,
category browsing,
brand/attribute filters,
price range,
shop filter,
region availability,
sort,
facets/aggregations,
cursor/search_after pagination.

Deep `from/size` pagination is prohibited for large result sets.

## 24.5 Query Understanding
Features may include:
- tokenizer/analyzer
- synonym dictionary
- typo correction
- search suggestion/autocomplete
- query rewrite
- category/brand intent extraction.

Any AI-based rewrite is advisory; final query remains auditable through QueryPlan snapshot.

## 24.6 Search Ranking
Rank inputs can include:
text relevance,
price competitiveness,
sales,
conversion,
review score,
shop quality,
freshness,
delivery capability,
risk/governance suppression,
personalization,
sponsored/ad score.

Sponsored results must be identified distinctly from organic ranking.

## 24.7 Search Governance
Hard-blocked/prohibited/offline offers must not appear.
Soft risk/quality signals may demote by versioned RankPolicy.

## 24.8 Recommendation Boundary
Recommendation is a derived decision/read-model system.
It cannot mutate Product/Trade/Payment facts.

Inputs:
impression,
view,
click,
search,
cart add/remove,
favorite,
shop follow,
coupon action,
purchase,
refund/aftersale,
review,
hide/dislike.

Outputs:
recommended offer IDs + reason/model/version/score.

## 24.9 Behavior Events
High-throughput behavior events use Kafka.
Event envelope includes:
eventId, user/session/device references,
eventType, objectType/objectId,
page/source/position,
occurredAt,
experimentId(optional),
privacy/consent context.

Do not put raw authentication/payment secrets into clickstream.

## 24.10 Recommendation Strategy
Supports:
- popular/trending
- collaborative filtering
- content-based
- session-based
- user-interest
- similar products
- complementary products
- shop recommendations
- re-ranking.

Serving combines candidate recall + filtering + ranking + business constraints.

## 24.11 Experiment
Recommendation/Search ranking experiments use:
Experiment
ExperimentVariant
Assignment
Exposure.
Assignment must be stable for configured subject key.

## 24.12 Review
Review belongs to verified transaction context.
One purchased item/eligible quantity follows platform review policy.

Review lifecycle:
DRAFT(optional)
→ PUBLISHED
→ HIDDEN / BLOCKED
→ APPEALED(optional).

Review content and moderation history are not physically erased as business fact.

## 24.13 Additional Review
Buyer may submit follow-up/additional review during policy window.
AdditionalReview is a child fact and does not overwrite original review.

## 24.14 Seller Reply
Merchant reply is separately versioned/audited.
Seller cannot edit buyer review.

## 24.15 Review Anti-Abuse
Risk signals:
verified purchase,
device/account graph,
review velocity,
text/image similarity,
seller-buyer relationship,
refund timing,
abnormal rating distribution,
campaign incentive.

Decision:
PASS / REVIEW / BLOCK.
Review aggregate remains owner of publish/hide state.

## 24.16 Review Summary
Product/shop score summaries are derived materialized read models:
review_count,
rating_average,
rating_distribution,
tag_counts,
verified_purchase_count.
They are rebuildable from review facts.

## 24.17 Favorites & Shop Follow
FavoriteOffer and ShopFollow are user-owned facts.
Idempotent unique relation:
user + object.
High-volume storage may be sharded by userId.

## 24.18 Notification
Business contexts publish notification-intent events.
Notification service owns:
recipient resolution,
preference,
template,
channel,
provider,
quiet hours,
dedup,
retry,
delivery receipt,
bounce/suppression.

Channels:
IN_APP / PUSH / SMS / EMAIL / WECHAT.

## 24.19 Customer Service
CustomerServiceCase links:
buyer,
merchant/shop,
Trade/MerchantOrder,
AfterSale/Dispute,
payment/refund,
shipment,
violation where applicable.

Case is support workflow, not a replacement for domain aggregate state.

## 24.20 Buyer-Seller IM
Conversation and Message are separate communication facts.
Messages may reference product/order/aftersale cards.
IM cannot directly transition Trade/Refund/Settlement.

Messages support:
server message ID,
client idempotency ID,
delivery/read status,
content type,
moderation status,
attachment references.

Sensitive payment credentials and prohibited content must not be transmitted.

## 24.21 User 360 Read Model
`Buyer360View` combines derived summaries:
profile/member,
recent orders,
favorite/follow summary,
coupon summary,
review summary,
support cases,
risk flags allowed for service staff,
lifecycle metrics.

This is CQRS read model only and must not become truth for money/order state.

## 24.22 Privacy & Retention
Search, behavior, recommendation and IM require explicit retention/access policies.
Data deletion/anonymization must respect legal hold and financial retention boundaries.

# 25. V3.0 Frozen Codegen Baseline

V3.0 is the first frozen implementation baseline.

No new top-level business domain may be introduced during TASK-001..038 implementation without a new ADR and baseline revision.

## 25.1 Frozen Architecture
- Java 21
- Spring Boot 3.x
- Spring Cloud microservices
- MyBatis-Plus
- MySQL 8
- Redis / Redisson
- RabbitMQ for transactional integration events
- Kafka for high-throughput behavior events
- Elasticsearch/OpenSearch for derived search read models
- MinIO/File Service for binary evidence/media
- XXL-JOB
- Flowable for human approval only
- ShardingSphere for capacity-driven hot-table sharding

## 25.2 Frozen Boundaries
Merchant != Shop
SPU != SKU != Offer
Trade != MerchantOrder != OrderItem
PaymentOrder != PaymentAttempt != PaymentTransaction
AfterSale decision != Reverse Logistics != Refund money fact
Payment success != Merchant settlement
Search/Recommendation/Buyer360 != source of transaction truth
Risk decision != business aggregate mutation
Governance violation != penalty execution
Customer Service / IM != direct domain-state mutation

## 25.3 Database Ownership
Each bounded context owns its database/schema logically.
Cross-service direct SQL join/write is forbidden.
`03-database/flyway/outbox` is a per-service integration-table template, not a shared global database.

## 25.4 Integration
Producer local TX:
business aggregate + Outbox.

Consumer local TX:
Inbox dedup + consumer domain mutation.

Cross-service recovery:
IntegrationTask / retry / reconciliation.

## 25.5 Sharding Freeze
Only hot families are frozen to explicit routing keys in V3.0:
- trade family: buyer_id
- payment/refund family: payment_no
- inventory: sku_id + warehouse_id
- settlement/payout: merchant_id
- review facts: offer_id
- cart/favorite/follow: user_id
- IM: buyer_id
- merchant seller-order read index: merchant_id

Physical shard counts are NOT frozen; they are capacity-test outputs.

## 25.6 API Contract
All OpenAPI operations:
- have unique operationId
- declare owner service
- use structured success/error envelopes
- define path parameters
- declare idempotency semantics
- do not expose arbitrary status update endpoints.

Money is Java BigDecimal/MySQL DECIMAL.
Server is authoritative for price, discount, stock, payment, refund and settlement.

## 25.7 Codegen Rule
Each task must read:
MASTER
Frozen Contract
service/table ownership
OpenAPI operation registry
Command/Event matrices
transaction/lock matrix
idempotency matrix
sharding routing
tests/release gates
task context.

If any required invariant is missing:
`SPEC-GAP: <description>`
Do not invent a destructive domain change.

## 25.8 Change Control
Breaking change requires:
1. ADR
2. affected Contract update
3. migration/API/event compatibility analysis
4. traceability update
5. validation PASS
6. baseline version increment.

V3.0 implementation must not silently modify frozen business facts.
