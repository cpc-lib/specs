# 一人 + AI 完整开发工期 — SPEC 7.9 Baseline

## 1. 估算前提

团队：

- 1 名全职开发者，Java/Spring Cloud 为主，可处理 React/UniApp
- AI Coding Assistant 作为 Pair Programmer
- 每周 5 个工作日
- 每日约 6～7 小时有效研发时间

AI 适合加速：

- DTO / Repository / SQL 初稿
- 单元测试与边界案例初稿
- React / UniApp 页面骨架
- 文档 / ADR / API Contract
- 错误分析与重构建议

人必须负责：

- 架构与业务边界
- 资金/账务正确性
- 设备协议与安全
- Code Review
- 真实环境联调
- 性能、安全、容灾与上线

## 2. 最新 Production V1 基线

SPEC 7.9 后正式调整为：

**46 周开发 + 4 周真实联调 Buffer = 50 周 / 250 人日**

原 47 周计划没有为以下 Operation Hardening 范围预留足够独立容量：

- Device Offline Detection
- Notification Escalation
- Inspection
- Spare Parts
- WorkOrder Attachments
- Technician UniApp

因此新增 3 周，而不是消耗原来的上线 Buffer。

## 3. 里程碑

| 里程碑 | 周期 | 可交付能力 |
|---|---:|---|
| Demo Vertical Slice | W1-W14 | 资产、模拟器、充电、计费、订单 |
| Payment MVP | W15-W17 | 支付、回调、UNKNOWN、退款 |
| Finance MVP | W18-W23 | Ledger、对账、结算、发票 |
| Operation MVP | W24-W29 | Alarm、Flowable、SLA、Offline、巡检、备件、Technician |
| Product MVP | W30-W37 | Admin、Merchant、Driver UniApp |
| Production Hardening | W38-W46 | OpenAPI、安全、性能、K8s、DR、AI Ops |
| Integration Buffer | W47-W50 | 真桩、支付/账单、发票、监管联调 |

## 4. 主计划

| Phase | 周 | 人日 | 主要内容 |
|---|---:|---:|---|
| P0 Foundation | W1-W2 | 10 | Maven、Nacos、Gateway、Flyway、Redis、Kafka、RabbitMQ、CI |
| P1 Asset | W3-W4 | 10 | Station/Charger/Connector |
| P2 IoT | W5-W7 | 15 | Netty、Auth、Heartbeat、Route、Command、Simulator |
| P3 Charging | W8-W11 | 20 | Session、Start/Stop、Recovery、Realtime |
| P4 Billing + Trade | W12-W14 | 15 | 峰平谷、Segment、Order、Replay |
| P5 Payment | W15-W17 | 15 | Payment、Callback、UNKNOWN、Refund |
| P6 Finance | W18-W21 | 20 | Ledger、Channel Bill、Reconciliation、Settlement |
| P7 Invoice/Wallet | W22-W23 | 10 | Invoice、Red Flush、Wallet |
| P8 Operation | W24-W26 | 15 | Alarm、WorkOrder、Flowable、SLA |
| **P8B Operation Hardening** | **W27-W29** | **15** | Offline、通知、巡检、备件、附件、Technician App |
| P9 Admin/Merchant | W30-W33 | 20 | React 后台完整化 |
| P10 Driver UniApp | W34-W37 | 20 | 地图、扫码、充电、订单、支付 |
| P11 OpenAPI/Regulatory | W38-W39 | 10 | OAuth/HMAC、第三方/监管 |
| P12 Hardening | W40-W42 | 15 | Security、Performance、Chaos、Observability |
| P13 K8s/DR/Release | W43-W44 | 10 | K8s、备份恢复、灰度、Runbook |
| P14 AI Ops/Packaging | W45-W46 | 10 | Ops Agent、文档、交付包 |
| Buffer | W47-W50 | 20 | 真桩、真实支付/账单、发票、监管 |

总计：**250 人日 / 50 周**。

## 5. P8B Operation Hardening — 15 人日

| Task | 人日 | AI 加速点 | 人工关键责任 |
|---|---:|---|---|
| Offline lifecycle | 2.0 | Redis/Kafka代码与测试初稿 | 重连竞态、误报策略 |
| Notification escalation | 2.0 | Policy/Worker模板 | on-call策略、渠道合规 |
| Inspection | 3.0 | CRUD/调度/页面 | 现场清单与流程 |
| Spare parts | 2.5 | SQL/API/UI | 库存并发、盘点制度 |
| Attachments | 1.5 | Storage adapter | 文件安全/对象存储 |
| Technician UniApp | 2.5 | 页面/API scaffold | 现场UX/真机 |
| QA/Docs/Review | 1.5 | case/document generation | Release Gate |
| **Total** | **15** |  |  |

## 6. 每周容量建议

普通阶段：

- 55% 新功能
- 20% 测试/修复
- 10% 重构
- 10% SPEC/ADR
- 5% 环境发布

财务/设备/运维并发阶段：

- 40% 新功能
- 30% 集成与故障场景
- 20% Review/一致性验证
- 10% 文档与 Runbook

## 7. 每日节奏

```text
09:00-09:30  回顾失败测试和阻塞
09:30-11:30  核心业务编码
13:30-15:00  DB/MQ/集成测试
15:00-16:30  AI 辅助生成次要代码 + 人工 Review
16:30-17:30  E2E/静态门禁/修复
17:30-18:00  TASK/SPEC/ADR/进度更新
```

每天结束必须留下可验证状态。

## 8. Critical Path

```text
Asset
→ IoT
→ ChargingSession
→ Billing
→ Order
→ Payment
→ Ledger
→ Reconciliation
→ Settlement
→ Operation
→ Product UI
→ Production Hardening
```

## 9. 风险与 Buffer

| 风险 | 典型影响 |
|---|---:|
| 真桩私有协议 | 1～3 周 |
| 微信/支付宝配置 | 3～7 天 |
| 渠道账单差异 | 3～7 天 |
| 发票服务商 | 3～7 天 |
| 监管地区差异 | 1～2 周 |
| Technician 现场流程变化 | 3～7 天 |
| K8s/网络/证书 | 3～7 天 |
| 财务规则变化 | 1～2 周 |

W47-W50 的 4 周 Buffer 保留给真实环境问题，不用于常规功能开发。

## 10. Definition of Done

每个 Task：

- Acceptance Criteria 通过
- Unit/Integration Test
- Flyway 可从空库重放
- 幂等/并发边界明确
- Tenant/Data Scope
- Audit / Trace / Metrics
- API 与 SPEC 一致
- 无 P0/P1 已知缺陷

资金、设备命令和运维高风险动作还必须具备恢复/审计路径。

## 11. 项目管理方式

```text
Backlog
→ Ready
→ Coding
→ Review
→ Test
→ Done
```

单 Task 目标：0.5～2 人日。

超过 3 人日必须拆分。

## 12. 当前 SPEC 7.9 状态

`Operation Hardening` 已进入 RC：

- Offline lifecycle：RC
- Notification escalation：RC
- Inspection：RC
- Spare parts：RC
- WorkOrder attachment：RC
- Technician UniApp：RC

仍需真实 Runtime Gate 才能记为 Verified。


## SPEC 8.0 Product MVP 进度

Product MVP 使用原计划 P9 + P10：

| Scope | 周期 | 人日 |
|---|---:|---:|
| Admin + Merchant | W30-W33 | 20 |
| Driver UniApp | W34-W37 | 20 |
| **Total** | **8 周** | **40 人日** |

因此总项目基线继续保持 **50 周 / 250 人日**。

AI 在 Product 阶段主要加速页面、DTO、表格、API client 和测试 case；人工时间集中在身份权限、DataScope、端到端 UX、异常态和真机验收。


## SPEC 8.1 Product Hardening 子计划

8.1 属于 Product MVP W30-W37 的 Hardening 工作，不额外扩展 50 周总计划。

| Task | 人日 | AI 适合 | 人工重点 |
|---|---:|---|---|
| Auth Session/Refresh | 3 | DTO、SQL、客户端重试 | 安全模型/竞态 |
| RBAC/Permission | 3 | CRUD/UI Scaffold | 权限边界 |
| Station Projection | 3 | Migration/Job 初稿 | 数据泄漏/升级兼容 |
| Driver WebSocket | 2 | Client scaffold | 真机/断线体验 |
| Frontend resilience | 2 | loading/error components | UX acceptance |
| E2E/security review | 2 | Case generation | Release decision |
| **Subtotal** | **15** |  |  |

Production V1 继续按 **50 周 / 250 人日**。


## SPEC 8.2 OpenAPI + Regulatory 子计划

对应正式计划：**W38-W39 / 10 人日**。

AI 加速范围：

- HMAC SDK / DTO / Feign client scaffold
- Flyway DDL 初稿
- Admin 表格/表单
- OpenAPI YAML
- E2E case generation
- Adapter contract boilerplate

人工重点：

- 外部身份与重放安全
- Partner DataScope
- Connector 越权启动防护
- Secret 生命周期
- SSRF / egress
- 监管平台真实协议差异
- 外部回调故障隔离
- 官方联调验收

SPEC 8.2 不新增总工期，Production V1 继续按 **50 周 / 250 人日**。

## SPEC 8.3 Security + Performance + Chaos 子计划

对应原计划：**W40-W42 / 15 人日**。

| 工作 | 人日 | AI 适合 | 人工重点 |
|---|---:|---|---|
| Sentinel / overload protection | 3 | rule scaffold / config | 阈值、误杀、降级策略 |
| Pool / timeout / backpressure | 2 | 配置/指标代码 | 饱和模型 |
| Prometheus / SLO / alerts | 2 | dashboard/rule scaffold | SLO 决策 |
| Load test / capacity | 3 | k6 场景生成 | 硬件条件、瓶颈归因 |
| Chaos / recovery | 3 | case/script scaffold | 数据一致性验收 |
| Security / key rotation | 2 | rewrap/tooling | 密钥生命周期 |
| **Total** | **15** |  |  |

Production V1 总基线继续保持 **50 周 / 250 人日**。
