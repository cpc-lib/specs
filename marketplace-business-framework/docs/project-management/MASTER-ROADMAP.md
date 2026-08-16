# Marketplace Master Roadmap

> 开发模式：1 人 + AI（Trae AI 全栈辅助）
> 总周期：80 周 / 40 个双周 Sprint / 约 20 个月
> 目标：Marketplace V3.0 SPEC 全量实现（38 个 TASK / 26 个业务模块 + 三端前端）
> 基线：`marketplace-v3.0` Frozen Codegen Baseline

## 与旧计划的差异

| 维度 | 旧计划 (v1) | 新计划 (v2) | 原因 |
|---|---|---|---|
| 总周期 | 96 周 / 24 个月 | 80 周 / 20 个月 | AI 并行生成前后端 + 样板代码，省 16 周 |
| ShardingSphere | P9 (Week 91) | P0 (Week 1) 设计 + P2 起生效 | 避免后期 DDL 大规模返工 |
| 前端节奏 | 仅周五 1 天 | AI 并行生成，每 Sprint 同步交付 | AI 生成前端页面近实时 |
| Risk 时机 | Phase 7 (Week 79) | Phase 1 (Week 5) | 遵循 SPEC Wave 2，Checkout 需要 Risk |
| Search 时机 | Phase 8 (Week 85) | Phase 3 (Week 19) | 遵循 SPEC Wave 4，可增量建设读模型 |
| Flash Sale | Phase 3 (Week 27) | Phase 8 (Week 63) | 遵循 SPEC Wave 11，依赖库存/促销/风控 |
| Phase 结构 | 10 Phase / 与 SPEC Wave 不对齐 | 10 Phase / 严格对齐 SPEC Wave 0-12 | 避免依赖倒置和返工 |
| 集成测试 | 无显式窗口 | 每 Phase 末尾 2-3 天集成验证 | 尽早发现跨服务问题 |

## 执行主线

```text
Foundation (Wave 0)
  ↓
User / Merchant / Risk (Wave 1-2)
  ↓
Shop / Product / Pricing / Inventory / Moderation (Wave 2-3)
  ↓
Promotion / Cart / Search / Checkout (Wave 4-5)
  ↓
Trade / Payment / Saga / Fulfillment / Logistics (Wave 6-7)
  ↓
Receive / AfterSale / Refund / Dispute (Wave 8)
  ↓
Settlement / Finance / Payout / Reconciliation / Invoice (Wave 9-10)
  ↓
Review / Notification / Recommendation (Wave 9-10-11)
  ↓
Seller Admin / Platform Admin / Buyer Frontend / Flash Sale / Procurement (Wave 11)
  ↓
Observability / Production Hardening (Wave 12)
  ↓
V1.0 Release
```

## TASK -> Phase 映射

### Phase 0 - Foundation (Week 1-4, Sprint 1-2)

**Wave 0: TASK-001** - Platform Foundation

| TASK | Scope | AI 生成 | 人工 Review |
|---|---|---|---|
| 001 | gateway/common/bom/config/trace/outbox/inbox | 框架代码、Docker Compose、三端骨架 | 架构决策、ShardingSphere 路由键设计 |

关键产出：
- Java 21 / Spring Boot 3.x / Spring Cloud 骨架可启动
- ShardingSphere 路由键设计落地（所有建表带分片键）
- Gateway / System / Security 基础
- Platform/Seller/Buyer 三端工程骨架
- Docker Compose（MySQL/Redis/MinIO/Nacos/RabbitMQ/Kafka/ES）
- Outbox/Inbox 模板代码

### Phase 1 - Identity & Merchant & Risk (Week 5-10, Sprint 3-5)

**Wave 1: TASK-002, TASK-003** + **Wave 2 (partial): TASK-029**

| TASK | Scope | 版本标注 |
|---|---|---|
| 002 | buyer identity/address/membership | - |
| 003 | merchant application/qualification/deposit | V2.5 |
| 029 | buyer/seller risk decision | V2.5, V2.6 |

关键产出：
- Buyer 完整账户体系
- Merchant 入驻/审核/激活（企业 + C2C）
- 类目准入、保证金子账、商户信用/等级
- Risk 基础设施（RuleSet/Decision/Case）- 后续 Checkout 依赖
- 三端对应页面（AI 并行生成）

### Phase 2 - Product & Shop & Inventory (Week 11-18, Sprint 6-9)

**Wave 2: TASK-004, TASK-005** + **Wave 3: TASK-006, TASK-007, TASK-008, TASK-030**

| TASK | Scope | 版本标注 |
|---|---|---|
| 004 | shop lifecycle/staff scope | V2.5 |
| 005 | platform catalog (category/brand/attribute) | V2.4 |
| 006 | product publishing/snapshots (SPU/SKU/Offer) | V2.4, V2.5 |
| 007 | price rules/snapshots | V2.4 |
| 008 | stock/reservation/ledger | - |
| 030 | product/review/report moderation | V2.5 |

关键产出：
- Shop 生命周期 + 商户员工权限
- 类目属性 schema + 品牌授权
- SPU/SKU/Offer 发布管线 + 不可变版本
- Pricing 引擎 + PriceBook + 快照
- 库存 stock/reservation/ledger + DB 条件扣减
- 商品治理：禁售/限制商品、IP/假货投诉
- ShardingSphere 生效：inventory (sku_id + warehouse_id)
- 商家可发布真正可售 Offer

### Phase 3 - Promotion & Cart & Search & Checkout (Week 19-24, Sprint 10-12)

**Wave 4: TASK-009, TASK-010, TASK-027** + **Wave 5: TASK-011**

| TASK | Scope | 版本标注 |
|---|---|---|
| 009 | cart persistence/read aggregation | - |
| 010 | campaign/coupon/budget | V2.2, V2.4 |
| 027 | ES read model | V2.6 |
| 011 | checkout session/token/revalidation | V2.4 |

关键产出：
- 促销引擎 + 优惠券 + 预算/配额
- 购物车 + 店铺分组
- Search ES 投影 + 查询理解 + 排名（增量建设）
- Checkout 会话 + 服务端重新校验（价格/促销/库存/可售性）
- 从商品到 Checkout 完整交易前链路

### Phase 4 - Trade & Payment & Saga & Logistics (Week 25-36, Sprint 13-18)

**Wave 6: TASK-012, TASK-013** + **Wave 7: TASK-014, TASK-015, TASK-016**

> 全项目第一关键阶段。AI 生成代码，人工必须逐行 Review 金额分配、Saga 补偿、支付回调。

| TASK | Scope | 版本标注 |
|---|---|---|
| 012 | trade/order/item/snapshot | V2.2 |
| 013 | order create saga (inventory/coupon/promotion compensation) | V2.1, V2.2, V2.4 |
| 014 | payment order/attempt/transaction | V2.1, V2.2 |
| 015 | fulfillment/package/shipment | V2.3 |
| 016 | carrier adapter/tracking | V2.3 |

关键产出：
- Trade/MerchantOrder/OrderItem 聚合 + 状态机
- 金额快照 + 分配 + funding responsibility
- Order Create Saga + RabbitMQ Outbox/Inbox 正式实现
- Payment 三层模型 + 微信/支付宝回调
- UNKNOWN 恢复机制
- FulfillmentOrder/Package/Shipment + 仓库路由
- 物流适配器 + 轨迹追踪
- ShardingSphere 生效：trade (buyer_id), payment (payment_no)

### Phase 5 - AfterSale & Refund & Dispute (Week 37-44, Sprint 19-22)

**Wave 8: TASK-017, TASK-018, TASK-019, TASK-020**

| TASK | Scope | 版本标注 |
|---|---|---|
| 017 | buyer/auto receive/order completion | V2.3 |
| 018 | refund-only/return-refund/exchange/repair | V2.3 |
| 019 | quota reservation/provider/refund facts | V2.1, V2.2, V2.3 |
| 020 | evidence/arbitration | V2.3 |

关键产出：
- 收货确认（买家确认/自动收货/拒收）
- AfterSale 完整决策（退款/退货退款/换货/维修）
- 逆向物流 + 退货质检 + 库存处置
- Refund 配额预留 + 退款交易 + UNKNOWN 保留
- Dispute 证据 + 仲裁 + 执行
- 完整订单生命周期闭环

### Phase 6 - Settlement & Finance Chain (Week 45-56, Sprint 23-28)

> 全项目最难阶段。资金链正确性 > 工期。

**Wave 9: TASK-022, TASK-023, TASK-026** + **Wave 10: TASK-024, TASK-025**

| TASK | Scope | 版本标注 |
|---|---|---|
| 022 | eligibility/batch/item/payable | V2.1, V2.2, V2.5 |
| 023 | merchant balance and marketplace accounting | V2.1, V2.2 |
| 024 | merchant payout/UNKNOWN | V2.2, V2.5 |
| 025 | payment/refund/settlement/payout recon | V2.2 |
| 026 | buyer/service invoices/red flush/email | - |

关键产出：
- 结算资格 + 批次 + 佣金快照
- 商户余额 + 复式记账 + 日终对账
- Payout + UNKNOWN + 资金冻结
- 全链路对账（Provider -> Payment -> Clearing -> Settlement -> Payout -> Ledger）
- 发票开具 + 红冲 + 重开
- ShardingSphere 生效：settlement (merchant_id)
- Payment -> Clearing -> Ledger -> Settlement -> Payable -> Payout -> Reconciliation 全资金链

### Phase 7 - Review & Notification & Recommendation (Week 57-62, Sprint 29-31)

**Wave 9: TASK-021** + **Wave 10: TASK-031** + **Wave 11: TASK-028**

| TASK | Scope | 版本标注 |
|---|---|---|
| 021 | verified-purchase review | V2.6 |
| 031 | SMS/email/push/in-app | V2.6 |
| 028 | behavior/read model integration | V2.6 |

关键产出：
- 评价体系 + 已购验证 + 追评 + 商家回复 + 反作弊
- 通知服务 + 多渠道 + 去重 + 重试
- Kafka 行为流 + 推荐召回/排序 + 实验/曝光
- 此阶段较轻，为 Phase 6 资金链 Review 留缓冲

### Phase 8 - Admin & Flash Sale & Procurement (Week 63-70, Sprint 32-35)

**Wave 11: TASK-032, TASK-033, TASK-034, TASK-035, TASK-036**

| TASK | Scope | 版本标注 |
|---|---|---|
| 032 | merchant back office | V2.6 |
| 033 | platform governance admin | V2.5, V2.6 |
| 034 | buyer web/app API integration | V2.6 |
| 035 | hot inventory/rate limit/queue | V2.4 |
| 036 | supplier/purchase/inbound | - |

关键产出：
- Seller Admin 全量页面（AI 批量生成）
- Platform Admin 全量页面（含治理/风控/资金/搜索/评价）
- Buyer Frontend 全量页面集成
- Flash Sale 秒杀（Redis/Lua 前置 + 持久化预留 + 对账）
- 自营采购/入库
- 三端 170-220 页面全部完成

### Phase 9 - Hardening (Week 71-78, Sprint 36-39)

**Wave 12: TASK-037, TASK-038**

| TASK | Scope | 版本标注 |
|---|---|---|
| 037 | tracing/metrics/runbook | - |
| 038 | chaos/performance/security/sharding | V2.1, V2.2, V2.5, V2.6 |

关键产出：
- 全链路追踪 + Metrics + Runbook
- ShardingSphere 全量验证
- 性能测试 + 安全测试 + 混沌测试
- ES 宕机/重建 + Kafka 重复/延迟 + 投影修复
- 24 条 E2E 验收链全部通过
- CI/CD + 备份恢复

### Phase 10 - Release (Week 79-80, Sprint 40)

关键产出：
- RC1 / RC2
- 全量回归
- Migration 演练
- V1.0 Release

## 每 Sprint 固定工作流

```text
SPEC 对应章节
-> State Machine / Invariants 确认
-> DDL / Flyway（AI 生成，人工确认分片键）
-> OpenAPI 实现（AI 生成）
-> Backend Domain/Application/Infrastructure（AI 生成，核心逻辑人工 Review）
-> Frontend 三端页面（AI 并行生成，非"仅周五"）
-> Unit Test + Integration Test（AI 生成骨架，人工补充边界用例）
-> Failure-path Test
-> 跨服务集成验证（Phase 末尾 2-3 天）
-> PROGRESS.md 更新
-> Release Note
```

## AI 能力加速点

AI 可近实时生成，无需分配独立 Sprint 时间：
- DTO / VO / Mapper / Converter / CRUD
- OpenAPI boilerplate
- React Table / Form / UniApp 页面骨架
- Test skeleton / Mock
- SQL migration draft
- Documentation / SPEC-IMPLEMENTATION-MAP

## 人工必须逐行 Review 的核心逻辑

```text
Money allocation / rounding residual
Inventory reservation / concurrency
Trade snapshot / amount conservation
Payment callback / signature / UNKNOWN
Refund quota / concurrent refund
Saga compensation
Settlement eligibility / hold
MerchantPayable / Payout
Double-entry Ledger
Reconciliation ¥0.01 rule
Idempotency key design
Sharding routing key
Security / Permission / DataScope
```

## 单人开发约束

- 同一时间只允许一个主业务目标（一个 Wave 内的 TASK 可并行生成）
- 不允许同时开启 3 个以上未收口的跨域任务
- 核心资金链、库存链、权限链必须人工 Review
- P9 开始后禁止新增大业务需求
- 每 Phase 末尾预留 2-3 天跨服务集成验证

## 不得牺牲的质量项

- 金额守恒
- 库存不超卖
- Trade / Payment / Refund / Payout 幂等
- UNKNOWN 恢复
- Settlement 正确性
- Double-entry Ledger
- Reconciliation
- Merchant / Shop 数据隔离
- Migration 可重复演练
- Failure-path tests

## 风险缓冲

正式计划：80 周
建议项目管理预留：额外 6-8 周外部缓冲
商业承诺：20-22 个月
若每周仅 25-30 小时：28-32 个月
