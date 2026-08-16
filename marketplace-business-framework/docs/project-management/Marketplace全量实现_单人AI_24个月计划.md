# Marketplace 全量实现计划（单人 + AI）

> 项目基线：Marketplace V3.0 SPEC + `marketplace-v3.0` Frozen Codegen Baseline
> 开发模式：**1 人全栈开发 + Trae AI 全栈辅助**
> 范围：**全部 38 个 TASK / 26 个业务模块 + Java Spring Cloud 后端 + React 平台管理后台 + React 商家后台 + UniApp 买家端 + 测试 + 部署 + Production Hardening**
> 正式工期：**80 周，约 20 个月**
> Sprint：**2 周 / Sprint，共 40 个 Sprint**
> 建议对外承诺：**20-22 个月**
> 若每周只能投入 25-30 小时：建议按 **28-32 个月** 估算。

---

# 1. AI 能力驱动的计划调整

与旧计划（96 周 / 24 个月）相比，本计划基于以下 AI 能力加速点压缩至 80 周：

## AI 近实时生成（不占独立 Sprint 时间）

- DTO / VO / Mapper / Converter / CRUD
- OpenAPI boilerplate 实现
- React Table / Form / 全量管理页面
- UniApp 页面骨架 + 业务页面
- Test skeleton / Mock / Test data
- SQL migration draft
- Documentation / SPEC-IMPLEMENTATION-MAP

## AI 并行能力

- 后端 Domain/Application/Infrastructure 代码与前端页面可同一 Sprint 内并行生成
- 同一 Wave 内多个 TASK 可并行生成（如 TASK-006/007/008 同时出码）
- 测试代码与业务代码同步生成

## 人工瓶颈（不可压缩）

以下工作仍需人工主导，决定项目工期下限：

- 核心逻辑 Review（金额/库存/支付/退款/结算/对账）
- 基础设施搭建与联调（Docker / MySQL / Redis / RabbitMQ / Kafka / ES）
- 支付渠道 sandbox 账号申请与联调
- 跨服务集成调试
- 安全审查
- 部署与运维

---

# 2. 80 周总览

| Phase | 周期 | Sprint | SPEC Wave | TASK | 主要内容 |
|---|---:|---:|---|---|---|
| P0 | W1-4 | S1-2 | Wave 0 | 001 | Foundation / ShardingSphere 设计 / 三端骨架 |
| P1 | W5-10 | S3-5 | Wave 1-2 | 002, 003, 029 | User / Merchant / Risk |
| P2 | W11-18 | S6-9 | Wave 2-3 | 004, 005, 006, 007, 008, 030 | Product / Pricing / Inventory / Shop / Moderation |
| P3 | W19-24 | S10-12 | Wave 4-5 | 009, 010, 011, 027 | Promotion / Cart / Search / Checkout |
| P4 | W25-36 | S13-18 | Wave 6-7 | 012, 013, 014, 015, 016 | Trade / Payment / Saga / Fulfillment / Logistics |
| P5 | W37-44 | S19-22 | Wave 8 | 017, 018, 019, 020 | Receive / AfterSale / Refund / Dispute |
| P6 | W45-56 | S23-28 | Wave 9-10 | 022, 023, 024, 025, 026 | Settlement / Finance / Reconciliation / Invoice |
| P7 | W57-62 | S29-31 | Wave 9-11 | 021, 028, 031 | Review / Notification / Recommendation |
| P8 | W63-70 | S32-35 | Wave 11 | 032, 033, 034, 035, 036 | Admin / Flash Sale / Procurement / 全量前端 |
| P9 | W71-78 | S36-39 | Wave 12 | 037, 038 | Hardening / Observability / Chaos |
| P10 | W79-80 | S40 | - | - | RC / V1.0 Release |

---

# 3. TASK -> Sprint 完整映射表

| TASK | 名称 | Wave | Phase | Sprint | Week | 版本标注 |
|---|---|---|---|---|---|---|
| 001 | Platform Foundation | 0 | P0 | S1-2 | W1-4 | - |
| 002 | Auth & User | 1 | P1 | S3 | W5-6 | - |
| 003 | Merchant Onboarding | 1 | P1 | S4-5 | W7-10 | V2.5 |
| 029 | Risk | 2 | P1 | S5 | W9-10 | V2.5, V2.6 |
| 004 | Shop | 2 | P2 | S6 | W11-12 | V2.5 |
| 005 | Category Brand Attribute | 2 | P2 | S6 | W11-12 | V2.4 |
| 006 | Product SPU SKU Offer | 3 | P2 | S7-8 | W13-16 | V2.4, V2.5 |
| 007 | Pricing | 3 | P2 | S8 | W15-16 | V2.4 |
| 008 | Inventory | 3 | P2 | S9 | W17-18 | - |
| 030 | Moderation | 3 | P2 | S9 | W17-18 | V2.5 |
| 009 | Cart | 4 | P3 | S10 | W19-20 | - |
| 010 | Promotion Coupon | 4 | P3 | S10-11 | W19-22 | V2.2, V2.4 |
| 027 | Search | 4 | P3 | S11 | W21-22 | V2.6 |
| 011 | Checkout | 5 | P3 | S12 | W23-24 | V2.4 |
| 012 | Trade Core | 6 | P4 | S13 | W25-26 | V2.2 |
| 013 | Order Create Saga | 6 | P4 | S14-15 | W27-30 | V2.1, V2.2, V2.4 |
| 014 | Payment | 7 | P4 | S15-16 | W29-32 | V2.1, V2.2 |
| 015 | Fulfillment | 7 | P4 | S17 | W33-34 | V2.3 |
| 016 | Logistics | 7 | P4 | S18 | W35-36 | V2.3 |
| 017 | Receive Complete | 8 | P5 | S19 | W37-38 | V2.3 |
| 018 | AfterSale | 8 | P5 | S20 | W39-40 | V2.3 |
| 019 | Refund | 8 | P5 | S21 | W41-42 | V2.1, V2.2, V2.3 |
| 020 | Dispute | 8 | P5 | S22 | W43-44 | V2.3 |
| 022 | Settlement | 9 | P6 | S23-24 | W45-48 | V2.1, V2.2, V2.5 |
| 023 | Merchant Ledger | 9 | P6 | S25 | W49-50 | V2.1, V2.2 |
| 024 | Payout | 10 | P6 | S26 | W51-52 | V2.2, V2.5 |
| 025 | Reconciliation | 10 | P6 | S27 | W53-54 | V2.2 |
| 026 | Invoice | 9 | P6 | S28 | W55-56 | - |
| 021 | Review | 9 | P7 | S29 | W57-58 | V2.6 |
| 031 | Notification | 10 | P7 | S30 | W59-60 | V2.6 |
| 028 | Recommendation | 11 | P7 | S31 | W61-62 | V2.6 |
| 032 | Seller Admin | 11 | P8 | S32 | W63-64 | V2.6 |
| 033 | Platform Admin | 11 | P8 | S33 | W65-66 | V2.5, V2.6 |
| 034 | Buyer Frontend | 11 | P8 | S34 | W67-68 | V2.6 |
| 035 | Flash Sale | 11 | P8 | S35 | W69-70 | V2.4 |
| 036 | Procurement | 11 | P8 | S35 | W69-70 | - |
| 037 | Observability | 12 | P9 | S36-37 | W71-74 | - |
| 038 | Production Hardening | 12 | P9 | S38-39 | W75-78 | V2.1, V2.2, V2.5, V2.6 |
| - | Release | - | P10 | S40 | W79-80 | - |

---

# 4. Phase 0：Foundation（Week 1-4 / Sprint 1-2）

## Sprint 1：工程基线 + ShardingSphere 设计

**TASK-001: Platform Foundation**

AI 生成：
- Java 21 / Spring Boot 3.x / Spring Cloud / Spring Cloud Alibaba Maven BOM
- marketplace-framework (common/web/security/mybatis/redis/file)
- marketplace-gateway / marketplace-system 骨架代码
- Docker Compose（MySQL/Redis/MinIO/Nacos/RabbitMQ/Kafka/ES）
- ShardingSphere 基础配置 + `SHARDING-ROUTING-FROZEN.yaml` 对应的路由键设计
- Outbox/Inbox 模板代码（per-service）
- Platform/Seller/Buyer 三端工程骨架

人工主导：
- 架构决策确认
- ShardingSphere 路由键设计 Review（trade: buyer_id, payment: payment_no, inventory: sku_id+warehouse_id, settlement: merchant_id, review: offer_id, cart/favorite: user_id, IM: buyer_id）
- Docker 环境搭建与验证

## Sprint 2：System / Security / 三端登录

**TASK-001 续**

AI 生成：
- PlatformUser / Role / Permission / Menu / Dictionary / Config 完整代码
- LoginLog / OperationLog
- JWT / RBAC / MerchantScope / ShopScope / DataScope 实现
- Platform React: Login + Layout + Menu + Router + Permission + Axios + Common Table/Form
- Seller React: Login + MerchantContext + ShopContext + Layout
- UniApp: Login + Request + Token + Store + Router + Tabbar + Common Components

人工 Review：
- 权限隔离逻辑（MerchantScope / ShopScope 不可被客户端伪造）
- JWT 安全配置

### M0 - Week 4

- 后端框架可启动
- 三端可登录
- Platform / Merchant / Buyer 身份隔离
- Merchant / Shop Scope 生效
- ShardingSphere 路由键设计落地

---

# 5. Phase 1：User / Merchant / Risk（Week 5-10 / Sprint 3-5）

## Sprint 3：User

**TASK-002: Auth & User**

AI 生成：
- BuyerAccount / BuyerProfile / BuyerAddress / 默认地址 / 用户状态 / 会员字段 / 安全信息 / 登录设备记录
- Flyway migration (user/V1, user/V2)
- OpenAPI (buyer-trade.yaml user 部分)
- UniApp: 登录 / 我的 / 用户资料 / 地址 / 设置
- Platform: 用户列表 / 详情 / 状态
- 单元测试 + 集成测试骨架

## Sprint 4-5：Merchant Onboarding + Risk

**TASK-003: Merchant Onboarding (V2.5)**
**TASK-029: Risk (V2.5, V2.6)**

AI 生成：
- MerchantApplication 全状态机 (DRAFT -> SUBMITTED -> IDENTITY_REVIEW -> QUALIFICATION_REVIEW -> RISK_REVIEW -> AGREEMENT_PENDING -> DEPOSIT_PENDING -> APPROVED -> ACTIVATING -> ACTIVE)
- MerchantVerificationProfile (企业 KYB / C2C 身份验证)
- CategoryAdmission / DepositAccount / DepositTransaction / MerchantCreditProfile / MerchantLevel / MerchantExitCase
- RiskRuleSet / RiskFeatureSnapshot / RiskDecision / RiskCase
- Risk 场景：LOGIN / MERCHANT_ONBOARDING / PRODUCT_PUBLISH / COUPON / FLASH_SALE / CHECKOUT / TRADE_SUBMIT / PAYMENT / REFUND / REVIEW / SETTLEMENT / PAYOUT
- Flyway migration (merchant/V1, V2, risk/V1, V2)
- OpenAPI (merchant.yaml, risk.yaml)
- Platform: 入驻申请 / 企业认证 / 个人认证 / 商户详情 / 保证金 / 信用 / 风控
- Seller: 入驻流程页面
- 测试 + 测试数据 (merchant-enterprise-onboarding.json, merchant-c2c-risk-reject.json, merchant-deposit-concurrency.json)

人工 Review：
- Merchant 申请状态机正确性
- 保证金子账（不可直接 balance overwrite）
- Risk 决策边界（Risk 返回决策，不直接变更业务域）
- C2C 延迟结算策略

### M1 - Week 10

- 用户、商户入驻、平台审核、风控基础设施全部完成

---

# 6. Phase 2：Product / Shop / Inventory（Week 11-18 / Sprint 6-9）

## Sprint 6：Shop + Category/Brand/Attribute

**TASK-004: Shop (V2.5)**
**TASK-005: Category Brand Attribute (V2.4)**

AI 生成：
- Shop 生命周期 / ShopStatus / MerchantMember / MerchantRole / MerchantPermission / ShopScope / DataScope / 员工邀请 / 店铺切换
- PlatformCategory / CategoryTree / CategoryAttributeDefinition (KEY/SALE/NORMAL/SEARCH/COMPLIANCE)
- Brand / BrandAuthorization (PENDING/ACTIVE/EXPIRED/REVOKED/REJECTED)
- Flyway (shop/V1, product/V1)
- Platform/Seller: 店铺管理 / 员工 / 类目树 / 属性 / 品牌授权

人工 Review：
- Shop 可售条件（须消费 Merchant ACTIVE + 类目准入 + 风控限制 + 保证金，不可仅凭 Shop ACTIVE）
- 品牌授权有效期校验

## Sprint 7-8：SPU / SKU / Offer / Pricing

**TASK-006: Product SPU SKU Offer (V2.4, V2.5)**
**TASK-007: Pricing (V2.4)**

AI 生成：
- SPU / SPUVersion / SKU / SKUVersion / Offer / OfferVersion（不可变版本）
- SKU 组合唯一性校验（归一化销售属性集）
- Offer 发布管线 (DRAFT -> VALIDATING -> PENDING_REVIEW -> APPROVED -> ONLINE -> OFFLINE -> BLOCKED)
- PriceBook / PriceBookItem / PriceSelectionPolicy / PricingSnapshot / PriceChangeRequest
- 区域/渠道/会员价 / 生效时间 / 历史价格
- Flyway (product/V2, pricing/V1, V2)
- OpenAPI (catalog.yaml, pricing.yaml, seller-product.yaml)
- Seller: SPU/SKU/Offer 管理 / 价格管理
- UniApp: 商品详情 / SKU Picker
- Platform: 商品审核 / SPU/SKU/Offer 列表

人工 Review：
- 不可变版本语义（发布后不可覆盖，变更须新建版本）
- Offer 发布须重新校验：商户类目准入 + 治理策略 + 风控决策 + 品牌授权
- 服务端权威价格（客户端提交价格永不作为权威）
- PricingSnapshot 快照规则

## Sprint 9：Inventory + Moderation

**TASK-008: Inventory**
**TASK-030: Moderation (V2.5)**

AI 生成：
- Warehouse / InventoryStock / InventoryReservation / InventoryLedger
- ON_HAND / AVAILABLE / RESERVED / LOCKED / IN_TRANSIT / DEFECTIVE / FROZEN
- DB 条件扣减 (UPDATE ... WHERE available >= ? AND ...)
- Redis 仅加速，DB ledger 为准
- 库存修复/对账基础
- ProductGovernancePolicy (PROHIBITED/RESTRICTED/LICENSE_REQUIRED/AGE_RESTRICTED/REGION_RESTRICTED/ALLOWED)
- 禁售/限制商品 / IP 投诉 / 假货投诉
- Flyway (inventory/V1, governance/V1)
- ShardingSphere inventory 分片生效 (sku_id + warehouse_id)
- Seller: 库存管理 / Warehouse
- Platform: 商品治理 / IP 投诉
- 测试 (inventory-concurrency.json, brand-authorization-expired.json, counterfeit-penalty.json)

人工 Review：
- 库存并发安全（DB 条件扣减，不可 Redis-only）
- 库存状态机 (AVAILABLE -> RESERVED -> COMMITTED 或 RESERVED -> RELEASED/EXPIRED)
- Moderation 不可直接 UPDATE 业务表

### M2 - Week 18

- 商品、SKU、Offer、价格、库存形成完整可售能力
- ShardingSphere inventory 分片生效

---

# 7. Phase 3：Promotion / Cart / Search / Checkout（Week 19-24 / Sprint 10-12）

## Sprint 10：Cart + Promotion Core

**TASK-009: Cart**
**TASK-010: Promotion Coupon (V2.2, V2.4)**

AI 生成：
- CartItem / Shop Group / Merchant Group / SKU 状态 / 商品失效 / 价格变化 / 库存变化
- Campaign / Rule / Scope / Promotion Compatibility
- Budget / Quota / Promotion Reservation
- CouponTemplate / CouponWallet / Claim / Lock / Use / Release / Expire
- Promotion 类型：DIRECT_DISCOUNT / PERCENTAGE_DISCOUNT / FULL_REDUCTION / FULL_DISCOUNT / N_FOR_FIXED_PRICE / N_ITEMS_DISCOUNT / BUY_X_GET_Y / GIFT / BUNDLE / MEMBER_PRICE / FLASH_SALE / FREE_SHIPPING
- Compatibility：STACKABLE / MUTUALLY_EXCLUSIVE / PRIORITY_EXCLUSIVE / BEST_BENEFIT_ONLY / REQUIRED_COMBINATION
- Funding responsibility (PLATFORM / MERCHANT / SHOP / BRAND / OTHER_APPROVED)
- Flyway (cart/V1, promotion/V1, V2, V3)
- OpenAPI (promotion.yaml)
- UniApp: 购物车 / 领券
- Platform: Campaign / Coupon / Budget / Quota
- Seller: 店铺活动 / Coupon
- 测试 (promotion-stack.json, promotion-exclusive.json, coupon-claim-concurrency.json, campaign-budget-concurrency.json)

人工 Review：
- 促销兼容性图 + 确定性 tie-break
- 预算/配额预留与释放幂等
- Funding responsibility 与 V2.2 契约一致

## Sprint 11：Search

**TASK-027: Search (V2.6)**

AI 生成：
- OfferSearchDocument 投影 + ES index/alias
- Outbox -> MQ -> Search Projection 消费者
- 版本化投影 (incomingVersion >= indexedVersion, CAS 保护)
- Query Understanding: tokenizer/analyzer/synonym/typo/suggestion/query rewrite
- Ranking: text relevance/price/sales/conversion/review/shop quality/freshness/delivery/risk suppression/personalization/sponsored
- Facets / search_after pagination
- Reindex 工具 + stale event 保护
- Flyway (search/V1)
- OpenAPI (search.yaml)
- UniApp: 搜索 / 搜索建议 / 搜索结果 / 筛选
- 测试 (search-version-order.json, search-stale-price-checkout.json)

人工 Review：
- 版本化投影 CAS 逻辑（旧事件不可覆盖新版本）
- Search 宕机不可阻塞 payment/refund/settlement
- Sponsored 结果须与 organic 明确区分

## Sprint 12：Checkout

**TASK-011: Checkout (V2.4)**

AI 生成：
- CheckoutSession / CheckoutToken / Address / Delivery / Price / Promotion / Coupon / Inventory / Merchant / Shop / Risk / Saleability
- 服务端重新校验全部可变事实
- Pricing + Promotion 求值并冻结所有引用版本/哈希
- SubmitTrade 前重算并拒绝过期/不兼容快照
- Flyway (checkout/V1)
- OpenAPI (buyer-trade.yaml checkout 部分)
- UniApp: Checkout / 优惠明细
- 测试 (pricing-precedence.json, discount-rounding.json)

人工 Review：
- Checkout 重新校验完整性（价格/促销/库存/可售性/风控）
- 快照冻结语义
- SubmitTrade 拒绝过期快照逻辑

### M3 - Week 24

- 从商品到 Checkout 的完整交易前链路完成

---

# 8. Phase 4：Trade / Payment / Saga / Logistics（Week 25-36 / Sprint 13-18）

> 全项目第一关键阶段。AI 生成代码，人工必须逐行 Review 金额分配、Saga 补偿、支付回调。

## Sprint 13：Trade Aggregate

**TASK-012: Trade Core (V2.2)**

AI 生成：
- Trade / MerchantOrder / OrderItem 聚合 + 状态机
- TradeNo / OrderNo 生成
- 跨店订单拆分
- ProductSnapshot / PricingSnapshot / PromotionSnapshot / PolicySnapshot / AddressSnapshot
- DiscountAllocation / FundingAllocation / OrderItemEconomicsSnapshot
- 金额守恒校验 + RoundingResidualPolicy
- Flyway (trade/V1, V2, V3, V4)
- OpenAPI (buyer-trade.yaml, seller-order.yaml)
- UniApp: 订单列表 / 订单详情
- Seller: 订单管理
- Platform: Trade / MerchantOrder / OrderItem 列表

人工 Review：
- **金额守恒**：sum(MerchantOrder payable) = Trade payable
- **分配不可变**：Trade 创建后不可从可变促销规则重新推导经济责任
- **Rounding residual** 显式分配

## Sprint 14-15：Order Create Saga + RabbitMQ

**TASK-013: Order Create Saga (V2.1, V2.2, V2.4)**

AI 生成：
- checkoutToken + idempotencyKey + TradeIdempotency
- Saga 步骤：create Trade -> reserve inventory -> lock coupons -> reserve promotion quota -> create payment relation -> WAITING_PAYMENT
- 补偿矩阵：每步失败的补偿动作
- RabbitMQ Outbox（producer local TX: business aggregate + Outbox）
- RabbitMQ Inbox（consumer local TX: Inbox dedup + consumer domain mutation）
- IntegrationTask / retry / reconciliation
- XXL-JOB: TradePaymentTimeoutJob / InventoryReservationExpireJob / OutboxRetry / IntegrationRetry
- Flyway (outbox/V1)
- 测试 (trade-payment-allocation.json, e2e-multi-merchant.json)

人工 Review：
- **Saga 补偿矩阵** 正确性（每步失败必须补偿已完成步骤）
- **幂等** 持久化 + 折扣分摊不可重算
- **Outbox/Inbox** 事务边界
- **赠品/捆绑库存** 须用标准 InventoryReservation

## Sprint 15-16：Payment

**TASK-014: Payment (V2.1, V2.2)**

AI 生成：
- PaymentOrder / PaymentAttempt / PaymentTransaction 三层模型
- payment_no 生成 + ShardingSphere 分片 (payment_no hash)
- Provider adapter（微信/支付宝/UnionPay sandbox）
- Callback: 签名验证 / merchant / paymentNo / currency / amount / status
- Duplicate callback safe
- TradePaymentAllocation
- UNKNOWN 查询恢复 (query-before-retry)
- Flyway (payment/V1, V2, V3)
- OpenAPI (payment.yaml)
- UniApp: 收银台 / 支付结果
- 测试 (payment-callback.json, payment-clearing.json, payout-unknown.json)

人工 Review：
- **回调验签** 完整性（签名/merchant/paymentNo/currency/amount/status）
- **Duplicate callback** 幂等
- **UNKNOWN never FAILED** - 保留状态直到权威查询
- **客户端支付成功不权威**
- Payment success 不直接写 merchant AVAILABLE

## Sprint 17：Fulfillment

**TASK-015: Fulfillment (V2.3)**

AI 生成：
- FulfillmentOrder / FulfillmentItem / 路由快照
- Package / PackageItem
- Shipment / TrackingEvent
- 部分发货 + 数量不变式 (orderedQty = unfulfilled + allocatedToPackage + cancelled + aftersaleAdjusted)
- 幂等发货与确认收货
- Warehouse Routing Matrix
- Flyway (fulfillment/V1, V2)
- OpenAPI (fulfillment.yaml)
- Seller: 发货 / Package / Shipment
- UniApp: 物流
- 测试 (partial-shipment.json, bundle-inventory.json)

人工 Review：
- **数量守恒** 不变式
- MerchantOrder state 不等于唯一 shipment state
- 路由快照不可变

## Sprint 18：Logistics

**TASK-016: Logistics (V2.3)**

AI 生成：
- LogisticsProviderAdapter 接口
- 归一化追加式轨迹（append-only TrackingEvent）
- 乱序保护（timestamp 排序）
- 派送异常 / 拒收 / 退回语义
- 逆向物流跟踪
- 测试 (delivery-rejection.json, digital-fulfillment.json)

人工 Review：
- Carrier DELIVERED != MerchantOrder completed
- 拒收不可盲目 CANCEL

### M4 - Week 36

- 跨店下单、支付、Saga、物流全部完成
- ShardingSphere trade/payment 分片生效

---

# 9. Phase 5：AfterSale / Refund / Dispute（Week 37-44 / Sprint 19-22）

## Sprint 19：Receive Complete

**TASK-017: Receive Complete (V2.3)**

AI 生成：
- CarrierDelivered / BuyerReceived / AutoReceived 三种事实
- 自动收货须加锁重校验
- 拒收走逆向路径
- DigitalFulfillment 完成策略
- 测试 (digital-fulfillment.json, delivery-rejection.json)

## Sprint 20：AfterSale

**TASK-018: AfterSale (V2.3)**

AI 生成：
- AfterSaleCase 决策聚合
- REFUND_ONLY / RETURN_REFUND / EXCHANGE / REPAIR
- ReturnOrder / ReverseShipment / ReturnInspection / ExchangeOrder / RepairOrder
- 超时时钟 (seller review / buyer return / seller receive / inspection / exchange shipment / repair SLA)
- 证据引用
- Flyway (aftersale/V1, V2)
- OpenAPI (aftersale.yaml)
- UniApp: 退款 / 退货退款 / 换货 / 维修 / 售后详情
- Seller: 售后管理
- 测试 (partial-return-inspection.json, exchange.json, return-refund.json)

人工 Review：
- AfterSale 决策 != Reverse Logistics != Refund money fact（三者分离）
- 超时命令幂等 + 重校验
- AfterSale 不可嵌入支付商调用

## Sprint 21：Refund

**TASK-019: Refund (V2.1, V2.2, V2.3)**

AI 生成：
- RefundOrder / RefundTransaction / RefundQuotaReservation
- refundable = paid - successful_refunds - reserved_refunds - non_refundable
- 并发安全（reserved + successful <= refundable）
- UNKNOWN 保留配额直到权威查询
- Refund reverse clearing (buyer cash / platform subsidy reversal / merchant discount reversal / shipping/tax reversal / ledger reversal)
- Flyway (payment/V2 refund 部分, finance/V2)
- 测试 (refund-concurrency.json, refund-before-settlement.json, refund-after-payout.json, return-stock-disposition.json)

人工 Review：
- **退款配额** 计算正确性（从不可变 paid/order allocation 推导）
- **并发退款** 不超退
- **UNKNOWN 保留配额**
- **Reverse clearing** 完整性

## Sprint 22：Dispute

**TASK-020: Dispute (V2.3)**

AI 生成：
- DisputeCase / 追加式 EvidenceRecord / 不可变 DecisionSnapshot
- 仲裁执行领域命令（不可直接 UPDATE 支付/库存/结算表）
- Appeal linkage
- Flyway (dispute/V1)
- OpenAPI (dispute 部分)
- UniApp: 纠纷详情
- 测试 (c2c-dispute.json)

人工 Review：
- 证据 append-only
- Decision 不可变 snapshot
- 仲裁执行通过领域命令，不直接改表

### M5 - Week 44

- 完整订单生命周期、售后、退换修、纠纷全部完成

---

# 10. Phase 6：Settlement / Finance / Reconciliation / Invoice（Week 45-56 / Sprint 23-28）

> 全项目最难阶段。资金链正确性 > 工期。

## Sprint 23-24：Settlement

**TASK-022: Settlement (V2.1, V2.2, V2.5)**

AI 生成：
- SettlementEligibility（first-class record: completion time / hold-until / blocking reason / eligibility version）
- SettlementBatch / SettlementItem
- CommissionRule 快照（effective-dated, snapshotted at settlement-relevant fact）
- SettlementAdjustment
- Funds Hold 检查（MerchantFundsHold scope: ALL_FUNDS / SETTLEMENT_BATCH / MERCHANT_ORDER / ORDER_ITEM / AMOUNT）
- exactly-once consume
- ShardingSphere settlement 分片 (merchant_id)
- Flyway (settlement/V1, V2, V3, V4, V5)
- OpenAPI (settlement.yaml)
- Platform: Eligibility / Batch / Settlement 列表
- 测试 (settlement.json, settlement-eligibility.json, funds-hold-payout.json)

人工 Review：
- **Eligibility** 是 first-class record，不是 payout 时临时推导
- **CommissionRule 快照** 不可重算历史
- **Funds Hold** 显式 scope，部分冻结不冻结无关经济范围
- **exactly-once** 消费

## Sprint 25：Merchant Ledger

**TASK-023: Merchant Ledger (V2.1, V2.2)**

AI 生成：
- MerchantBalanceAccount / MerchantBalanceLedger (append-only)
- MerchantPayable (PENDING -> AVAILABLE/ELIGIBLE -> SETTLING -> PAID, may be FROZEN/NEGATIVE)
- PaymentClearingRecord / PaymentClearingAllocation
- RefundReverseAllocation
- Flyway (finance/V1, V2, V3)
- Platform: Merchant Balance / Payable / Clearing
- 测试 (payout-concurrency.json)

人工 Review：
- **余额变动** 必须通过 Ledger，禁止直接 repair UPDATE
- **Payment success** 不直接写 merchant AVAILABLE
- **Clearing** 正确分配到 trade/order economic buckets

## Sprint 26：Payout

**TASK-024: Payout (V2.2, V2.5)**

AI 生成：
- PayoutReservation / PayoutOrder / PayoutTransaction
- Payout UNKNOWN 保留 reservation + provider query
- Duplicate payout protection
- Withdrawal (optional, only when PSP/bank legally supports)
- 资金冻结检查 (HOLD/REVIEW 创建显式事实)
- Flyway (settlement payout 部分)
- Platform: Payout 管理
- 测试 (payout-unknown.json, payout-concurrency.json)

人工 Review：
- **先预留后调用** PSP/bank
- **UNKNOWN 保留 reservation**
- **Duplicate payout** 幂等
- **资金冻结** 显式检查

## Sprint 27：Reconciliation

**TASK-025: Reconciliation (V2.2)**

AI 生成：
- Provider Statement 导入
- Match / Mismatch / Missing / Duplicate / Exception
- Manual resolution
- Re-run
- ¥0.01 差异检测
- Daily Close + Control Check
- Flyway (reconciliation/V1, V2)
- Platform: Reconciliation / Daily Close / Exception
- 测试 (daily-close-mismatch.json)

人工 Review：
- **全链路对账**：Provider -> PaymentTransaction -> PaymentClearing -> TradeAllocation -> MerchantPending -> Settlement -> MerchantPayable -> Payout -> Ledger
- **¥0.01 差异** 不可忽略

## Sprint 28：Invoice

**TASK-026: Invoice**

AI 生成：
- Invoice Apply / Issue / Query / Red Flush / Reissue / Delivery
- Flyway (invoice 部分)
- UniApp: 发票
- Platform: Invoice 管理

### M6 - Week 56

- Payment -> Clearing -> Ledger -> Settlement -> Payable -> Payout -> Reconciliation -> Invoice 全资金链完成

---

# 11. Phase 7：Review / Notification / Recommendation（Week 57-62 / Sprint 29-31）

> 此阶段较轻，为 Phase 6 资金链 Review 留缓冲。

## Sprint 29：Review

**TASK-021: Review (V2.6)**

AI 生成：
- Review (verified purchase) + AdditionalReview + SellerReviewReply
- Review lifecycle: DRAFT -> PUBLISHED -> HIDDEN/BLOCKED -> APPEALED
- ReviewAntiAbuse (verified purchase / device graph / review velocity / text similarity / seller-buyer relationship / refund timing / abnormal rating / campaign incentive)
- ReviewSummary (review_count / rating_average / rating_distribution / tag_counts / verified_purchase_count)
- Rebuildable from review facts
- Flyway (review/V1, V2, V3)
- OpenAPI (review.yaml)
- UniApp: 评价 / 追评
- Seller: 评价管理 / 回复
- 测试 (review-verified-purchase.json, review-non-purchase.json)

## Sprint 30：Notification

**TASK-031: Notification (V2.6)**

AI 生成：
- NotificationPreference / Message / Delivery
- Channels: IN_APP / PUSH / SMS / EMAIL / WECHAT
- Provider adapter
- 免打扰时段 / 去重 / 重试 / 回执 / 退信 / 分类退订
- Flyway (notification/V1)
- UniApp: 消息中心 / 通知偏好
- 测试 (notification-dedup.json)

## Sprint 31：Recommendation

**TASK-028: Recommendation (V2.6)**

AI 生成：
- Kafka behavior event pipeline (impression/view/click/search/cart/favorite/follow/coupon/purchase/refund/review/hide)
- Recommendation strategy: trending / collaborative filtering / content-based / session / user-interest / similar / complementary / shop / re-ranking
- Experiment / ExperimentVariant / Assignment / Exposure (stable for subject key)
- Hard filter: 商品/治理过滤
- Flyway (recommendation/V1)
- UniApp: 推荐位
- 测试 (recommendation-experiment.json, behavior-dedup.json)

人工 Review：
- 推荐不可变更 Product/Trade/Payment 事实
- Experiment assignment 稳定性
- 行为事件不含敏感凭证

### M7 - Week 62

- 评价、通知、推荐完成，26 个模块接近全部完成

---

# 12. Phase 8：Admin / Flash Sale / Procurement（Week 63-70 / Sprint 32-35）

> AI 批量生成三端全量页面。

## Sprint 32：Seller Admin

**TASK-032: Seller Admin (V2.6)**

AI 生成：
- Dashboard / 店铺 / 员工 / 角色 / 权限
- 商品 / SPU/SKU/Offer / 价格 / 库存
- 营销 / Coupon / Flash Sale
- 订单 / 发货 / Package / Shipment
- 售后 / Return / Exchange / Repair / Dispute
- 评价 / Seller Reply
- Settlement / Payable / Balance / Payout / Reconciliation / Invoice
- Customer Service / IM
- Merchant Credit / Deposit / Category Admission / Appeal
- 目标 50-65 页面

## Sprint 33：Platform Admin

**TASK-033: Platform Admin (V2.5, V2.6)**

AI 生成：
- System: 用户/角色/权限/菜单/字典/参数/日志
- Merchant: 入驻/认证/详情/店铺/准入/保证金/信用/退出
- Product: 类目/属性/品牌/SPU/SKU/Offer/审核/合规/授权
- Pricing/Inventory: PriceBook/价格变更/Warehouse/Stock/Reservation/Ledger
- Promotion: Campaign/Rule/Coupon/Budget/Quota/FlashSale/Gift/Bundle
- Trade/Payment: Trade/MerchantOrder/OrderItem/Payment/Refund
- Fulfillment: FulfillmentOrder/Package/Shipment/Tracking
- AfterSale: Refund/Return/Exchange/Repair/Dispute/Arbitration
- Settlement/Finance: Eligibility/Batch/Payable/Balance/FundsHold/Payout/Ledger/Clearing/DailyClose/Reconciliation/Invoice
- Governance: Risk/Violation/Penalty/Appeal/IP Complaint/Merchant Exit
- CX: Review/Search/Recommendation experiment/Notification/Customer Service/Buyer360
- 目标 75-95 页面

## Sprint 34：Buyer Frontend

**TASK-034: Buyer Frontend (V2.6)**

AI 生成：
- 首页/类目/搜索/搜索建议/搜索结果/筛选
- 商品详情/SKU Picker/Shop/Review
- Cart/Coupon/Checkout/Address/Delivery/Cashier/Payment Result
- Order List/Order Detail/Logistics/Confirm Receive
- Refund/Return Refund/Exchange/Repair/AfterSale Detail/Dispute
- Review Submit/Additional Review
- Favorite/Follow Shop/Recently Viewed/Recommendation
- Notification Center/Customer Service/Buyer-Seller IM
- Profile/Address/Settings/Notification Preference
- 目标 45-60 页面

## Sprint 35：Flash Sale + Procurement

**TASK-035: Flash Sale (V2.4)**
**TASK-036: Procurement**

AI 生成：
- Flash Sale: Redis/Lua 前置闸门 + 持久化秒杀预留 + 限购 + 排队 + 补偿 + 对账
- 活动配额与仓库 InventoryReservation 分离
- Procurement: supplier/purchase/inbound
- 测试 (flash-sale-consistency.json)

人工 Review：
- **秒杀配额 != 库存预留**（分离）
- **Redis 前置 + DB ledger 对账**
- **失败/超时** 幂等释放

### M8 - Week 70

- 26 个业务模块全部完成核心实现
- 三端 170-220 页面全部完成

---

# 13. Phase 9：Production Hardening（Week 71-78 / Sprint 36-39）

> 此阶段禁止新增业务。

## Sprint 36-37：Observability + Integration

**TASK-037: Observability**

AI 生成：
- 全链路 tracing (Spring Cloud Sleuth / Zipkin)
- Metrics (Prometheus / Grafana)
- Runbook
- 日志聚合 (ELK)
- 健康检查

## Sprint 38-39：Chaos / Performance / Security / Sharding

**TASK-038: Production Hardening (V2.1, V2.2, V2.5, V2.6)**

验证：
- ShardingSphere 全量路由验证 (binding table / route key / no broadcast hot write)
- MQ: duplicate / retry / DLQ / replay / outbox repair
- Redis: cache penetration / breakdown / avalanche / hot key / lock failure
- 性能: Product / Search / Cart / Checkout / SubmitTrade / InventoryReservation / PaymentCallback / FlashSale / Settlement / Reconciliation
- 安全: JWT / RBAC / MerchantScope / ShopScope / IDOR / SQL injection / XSS / SSRF / file upload / MinIO / payment signature / replay / rate limit / sensitive data
- 容灾: MySQL failure / Redis failure / RabbitMQ failure / Kafka lag / ES failure / provider timeout / UNKNOWN recovery
- 24 条 E2E 验收链全部通过
- CI/CD + backup / restore

### M9 - Week 78

- 全链路压测、安全、容灾通过
- 24 条 E2E 全部通过

---

# 14. Phase 10：Release（Week 79-80 / Sprint 40）

- RC1 全量回归
- DB migration 演练
- Docker 部署 + Production 配置
- backup / restore 验证
- CI/CD pipeline
- health checks / logs / metrics / trace
- permission review / error-code review
- API docs / admin manual / seller manual / buyer manual / deployment manual / incident runbook
- RC2
- V1.0 Release

### M10 - Week 80

- V1.0 Production Release

---

# 15. 前端全量页面范围

## Platform React: 75-95 页

System / Merchant / Product / Pricing/Inventory / Promotion / Trade/Payment / Fulfillment / AfterSale / Settlement/Finance / Governance / CX

## Seller React: 50-65 页

Dashboard / 店铺 / 员工 / 商品 / 营销 / 订单 / 发货 / 售后 / 评价 / Settlement / CS/IM / Credit/Deposit

## UniApp Buyer: 45-60 页

首页 / 类目 / 搜索 / 商品 / Cart / Checkout / 支付 / 订单 / 物流 / 售后 / 评价 / 收藏 / 通知 / CS/IM / 个人

> AI 并行生成：前端页面与后端 API 同 Sprint 交付，不采用"仅周五"模式。

---

# 16. Sprint Definition of Done

- [ ] SPEC 对应章节确认
- [ ] State Machine 确认
- [ ] DDL / Flyway 完成（分片键正确）
- [ ] OpenAPI 完成
- [ ] Error Code 完成
- [ ] Permission 完成
- [ ] Domain Aggregate / Policy 完成
- [ ] Application Command / Query 完成
- [ ] Repository / Persistence 完成
- [ ] Idempotency 完成
- [ ] Transaction Boundary 完成
- [ ] MQ / Saga 完成（需要时）
- [ ] Job 完成（需要时）
- [ ] Platform React 完成
- [ ] Seller React 完成
- [ ] UniApp 完成
- [ ] Unit Test
- [ ] Integration Test
- [ ] Failure-path Test
- [ ] 跨服务集成验证（Phase 末尾）
- [ ] SPEC-IMPLEMENTATION-MAP 更新
- [ ] 无 P0 / P1 Bug
- [ ] Release Note
- [ ] Git Tag

---

# 17. AI 使用策略

## AI 全量生成

- DTO / VO / Mapper / Converter / CRUD
- OpenAPI boilerplate
- React Table / Form / 全量管理页面
- UniApp 页面骨架 + 业务页面
- Test skeleton / Mock / Test data
- SQL migration draft
- Documentation / SPEC-IMPLEMENTATION-MAP
- repetitive adapters
- 三端前端页面（与后端同 Sprint 并行）

## 人工必须逐行 Review

- Trade amount allocation / rounding residual
- Promotion compatibility / tie-break
- Inventory reservation / concurrency
- Payment / callback / signature / UNKNOWN
- Refund quota / concurrent refund / reverse clearing
- Saga compensation matrix
- Settlement eligibility / hold
- MerchantPayable / Payout
- Double-entry Ledger
- Reconciliation ¥0.01 rule
- Idempotency key design
- Sharding routing key
- Security / Permission / DataScope
- Risk / Governance actions

---

# 18. 进度衡量

不使用代码行数。使用：

> 完成多少条可验收业务链

24 条关键 E2E（见 MILESTONES.md）

---

# 19. 风险缓冲

正式计划：80 周
建议项目管理预留：额外 6-8 周外部缓冲
商业承诺：20-22 个月
若每周仅 25-30 小时：28-32 个月

---

# 20. 最终结论

基于 AI 全栈辅助能力：

- AI 近实时生成样板代码和前端页面（省 ~8 周）
- AI 并行生成同 Wave 内多个 TASK（省 ~4 周）
- ShardingSphere 前移避免返工（省 ~4 周）

正式采用：

# **80 周 / 20 个月 / 40 个双周 Sprint**

时间节点：

- Week 4：Foundation + ShardingSphere 设计完成
- Week 10：商户 + 风控基础设施完成
- Week 18：商品/价格/库存可售能力完成
- Week 24：Checkout 交易前链路完成
- Week 36：Trade / Payment / Saga 完成
- Week 44：履约售后退换修纠纷完成
- Week 56：资金财务对账全链路完成
- Week 62：评价/通知/推荐完成
- Week 70：26 个模块 + 三端全量页面完成
- Week 78：Production Hardening 完成
- Week 80：V1.0 Release

> **Marketplace V3.0 SPEC 是唯一业务事实源，AI 用于提高实现效率，但关键交易、资金、库存和权限逻辑必须由你最终审核。**
