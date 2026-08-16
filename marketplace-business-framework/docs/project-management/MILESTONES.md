# Milestones

## M0 - Week 4 - Foundation Ready

验收：

- Gateway / System 可启动
- Platform / Seller / Buyer 三端可登录
- JWT / RBAC 生效
- MerchantScope / ShopScope 生效
- MySQL / Redis / MinIO / Nacos 基础环境可用
- ShardingSphere 路由键设计完成，所有建表带分片键
- Docker Compose 一键启动基础设施
- Outbox/Inbox 模板代码就绪

对应：TASK-001 / Wave 0

## M1 - Week 10 - Identity & Merchant & Risk Ready

验收：

- Buyer 完整基础账户（登录/资料/地址/会员）
- Merchant 入驻 / 审核 / 激活（企业 + C2C）
- Shop 生命周期
- Merchant Member / Role / Permission
- Category Admission / Deposit / Credit 基础能力
- Risk 基础设施可用（RuleSet / Decision / Case）
- 平台审核流程可跑通

对应：TASK-002, TASK-003, TASK-029 / Wave 1-2

## M2 - Week 18 - Saleable Product Ready

验收：

- Category / Attribute / Brand 完整 schema
- SPU / SKU / Offer 发布管线 + 不可变版本
- Pricing 引擎 + PriceBook + 快照
- Inventory stock / reservation / ledger + DB 条件扣减
- 商家可发布真正可售 Offer
- 买家可浏览并选择 SKU
- 商品治理：禁售/限制/IP 投诉可用
- ShardingSphere inventory 分片生效

对应：TASK-004, TASK-005, TASK-006, TASK-007, TASK-008, TASK-030 / Wave 2-3

## M3 - Week 24 - Checkout Ready

验收：

- Promotion / Coupon / Budget / Quota 完整
- Cart 持久化 + 店铺分组
- Search ES 投影 + 查询 + 排名（增量可用）
- Checkout 会话 + 服务端重新校验
- Price / Promotion / Inventory / Saleability 服务端校验通过
- 从商品到 Checkout 完整交易前链路打通

对应：TASK-009, TASK-010, TASK-011, TASK-027 / Wave 4-5

## M4 - Week 36 - Commerce Transaction Ready

验收：

- Cross-shop Trade 创建
- MerchantOrder / OrderItem + 金额快照
- Order Create Saga + 补偿（库存/优惠券/促销）
- Payment 三层模型 + 微信/支付宝回调
- RabbitMQ Outbox / Inbox 正式运行
- Payment / Refund UNKNOWN 恢复
- FulfillmentOrder / Package / Shipment
- 物流适配器 + 轨迹追踪
- ShardingSphere trade/payment 分片生效

对应：TASK-012, TASK-013, TASK-014, TASK-015, TASK-016 / Wave 6-7

## M5 - Week 44 - Order Lifecycle Ready

验收：

- 收货确认（买家/自动/拒收）
- AfterSale 完整决策（退款/退货退款/换货/维修）
- 逆向物流 + 退货质检 + 库存处置
- Refund 配额预留 + 并发安全 + UNKNOWN
- Dispute 证据 + 仲裁 + 执行
- 完整订单生命周期闭环

对应：TASK-017, TASK-018, TASK-019, TASK-020 / Wave 8

## M6 - Week 56 - Financial Closure Ready

验收：

- Settlement Eligibility + 批次 + 佣金快照
- MerchantPayable + 商户余额 + 复式记账
- Payout + UNKNOWN + 资金冻结
- Daily Close + Control Check
- 全链路对账（Provider -> Payment -> Clearing -> Settlement -> Payout -> Ledger）
- 发票开具 + 红冲 + 重开
- ShardingSphere settlement 分片生效
- ¥0.01 对账差异检测

对应：TASK-022, TASK-023, TASK-024, TASK-025, TASK-026 / Wave 9-10

## M7 - Week 62 - CX Base Ready

验收：

- Review 已购验证 + 追评 + 商家回复 + 反作弊
- Notification 多渠道 + 去重 + 重试
- Kafka 行为流 + 推荐召回/排序 + 实验/曝光
- 为 Phase 6 资金链 Review 提供缓冲

对应：TASK-021, TASK-028, TASK-031 / Wave 9-10-11

## M8 - Week 70 - All Modules Implemented

验收：

- Seller Admin 全量页面完成
- Platform Admin 全量页面完成
- Buyer Frontend 全量页面完成
- Flash Sale 秒杀（Redis/Lua + 持久化 + 对账）
- 自营采购/入库
- 26 个业务模块不存在长期空壳模块
- 三端 170-220 页面全部完成

对应：TASK-032, TASK-033, TASK-034, TASK-035, TASK-036 / Wave 11

## M9 - Week 78 - Production Hardening Ready

验收：

- 全链路追踪 + Metrics + Runbook
- ShardingSphere 全量验证通过
- 性能测试达标
- 安全测试通过
- 混沌测试通过（ES 宕机/Kafka 重复/Redis 故障/provider timeout）
- 24 条 E2E 验收链全部通过
- CI/CD + 备份恢复验证

对应：TASK-037, TASK-038 / Wave 12

## M10 - Week 80 - Production Ready V1.0

验收：

- RC1 / RC2 回归通过
- Migration 演练通过
- P0 / P1 Bug = 0
- Production 配置完成
- V1.0 Release

## 24 条关键 E2E 验收链

1. Merchant onboarding -> Shop active
2. Product -> Offer published
3. Inventory -> Saleable
4. Coupon claim -> Checkout
5. Cross-shop Trade
6. Payment callback
7. Partial shipment
8. Buyer receive
9. Refund only
10. Return + Inspection + Refund
11. Exchange
12. Repair
13. Dispute
14. Settlement eligibility
15. Merchant payable
16. Payout
17. Reconciliation
18. Invoice red flush + reissue
19. Review + Seller reply
20. Violation + Penalty + Appeal
21. Search stale-event recovery
22. IM duplicate message
23. Notification retry
24. Buyer360 rebuild

## 里程碑与 SPEC Wave 对应关系

| 里程碑 | 周 | SPEC Wave | TASK 数量 |
|---|---:|---|---:|
| M0 Foundation | 4 | Wave 0 | 1 |
| M1 Identity & Risk | 10 | Wave 1-2 | 3 |
| M2 Saleable Product | 18 | Wave 2-3 | 6 |
| M3 Checkout | 24 | Wave 4-5 | 4 |
| M4 Commerce Transaction | 36 | Wave 6-7 | 5 |
| M5 Order Lifecycle | 44 | Wave 8 | 4 |
| M6 Financial Closure | 56 | Wave 9-10 | 5 |
| M7 CX Base | 62 | Wave 9-11 | 3 |
| M8 All Modules | 70 | Wave 11 | 5 |
| M9 Hardening | 78 | Wave 12 | 2 |
| M10 V1.0 | 80 | - | - |
| **合计** | **80** | **Wave 0-12** | **38** |
