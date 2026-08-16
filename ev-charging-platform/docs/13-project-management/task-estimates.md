# Task Estimates

## SPEC 7.5 当前 Payment Slice

| Task | 预计 | Owner | AI 加速点 | Exit Gate |
|---|---:|---|---|---|
| PAY-001 Payment Schema | 0.5d | Dev | DDL review | Flyway 可迁移 |
| PAY-002 Core Order Snapshot | 0.5d | Dev | DTO/Feign | 租户隔离 |
| PAY-003 PaymentOrder | 1.0d | Dev | CRUD/test | requestId 幂等 |
| PAY-004 Mock Gateway | 0.5d | Dev | adapter | PENDING 可创建 |
| PAY-005 Callback | 1.5d | Dev | 并发测试 | 100 次回调一次效果 |
| PAY-006 Event Outbox | 1.0d | Dev | publisher/test | Kafka at-least-once |
| PAY-007 Core Projection | 1.0d | Dev | consumer test | Order PAID |
| PAY-008 Refund Reservation | 1.0d | Dev | boundary cases | 不超退 |
| FIN-001 Ledger Schema | 0.5d | Dev | DDL | append-only |
| FIN-002 Double Entry | 1.0d | Dev | property tests | Debit=Credit |
| FIN-003 Payment Ledger Consumer | 1.0d | Dev | event test | Inbox 幂等 |
| QA-001 Payment E2E | 2.0d | Dev | case generation | 全链路通过 |

预计：**11.5 人日**，建议预留 **3.5 人日**处理依赖、并发、渠道差异和回归，合计 **15 人日 / 3 周**。

## SPEC 7.6 当前 Finance Slice

| Task | 预计 | AI 加速点 | Exit Gate |
|---|---:|---|---|
| FIN-004 Finance Payment/Refund Facts | 1.0d | event DTO / consumer test | 不跨库 JOIN Payment |
| FIN-005 Channel Bill Import | 1.0d | parser/template | 原始/规范化批次幂等 |
| FIN-006 Exact Reconciliation | 2.0d | matcher/property cases | 1 fen 不容差 |
| FIN-007 Difference Case | 1.0d | workflow/test cases | 差异不得结算 |
| FIN-008 Settlement Source | 1.0d | invariant review | 仅 MATCH 产生 |
| FIN-009 Rule Version | 1.0d | DDL/API | Published version 固化 |
| FIN-010 Settlement Engine | 2.0d | property tests | 每一分钱守恒 |
| FIN-011 Admin Finance UI | 1.5d | React 表单/表格 | 可导入/对账/结算 |
| QA-002 Finance E2E | 2.5d | case generation | MATCH/差错/结算闭环 |

本 Slice 预计 **13 人日**；建议再预留 **3～5 人日**用于真实微信/支付宝账单格式、T+1 日切、退款跨日与财务回归，因此规划 **16～18 人日**。该时间已包含在 S6 Finance 的 20 人日基线及后续 Buffer 中，不上调 Production V1 的 47 周总基线。

## SPEC 7.7 Finance Hardening

| Task | 预计 | AI 加速点 | Exit Gate |
|---|---:|---|---|
| FIN-012 T+1 Schedule | 1.0d | Scheduler/SQL scaffold | 多实例 requestId 幂等 |
| FIN-013 Raw Bill Archive | 1.5d | parser/archive adapter | SHA-256 + archive before normalize |
| FIN-014 Adjustment | 2.0d | DDL/API/test cases | 原事实不可变 + maker-checker |
| FIN-015 Reversal | 1.0d | reversal cases | append-only + single active reversal |
| FIN-016 Settlement Approval | 2.0d | workflow/UI | maker != checker |
| FIN-017 Settlement Ledger Posting | 1.5d | ledger entries/property test | Debit = Credit before SETTLED |
| FIN-018 Invoice Provider | 2.0d | adapter/mock/state tests | provider call outside DB tx |
| FIN-019 Invoice Red Flush | 1.0d | state cases | red flush idempotent |
| FIN-020 Admin Finance Hardening UI | 1.5d | React tables/forms | schedule/adjust/approve/invoice usable |
| QA-003 Finance Hardening Regression | 2.5d | boundary generation | 1 fen / retry / concurrency regression |

工程工作量约 **16 人日**；真实账单下载、生产对象存储、真实发票 Provider 与结算付款额外预留 **2～4 人日**到后续联调 Buffer。总计划仍维持 **47 周 / 235 人日**。


## SPEC 7.9 Operation Hardening

| Task | 预计 | Exit Gate |
|---|---:|---|
| OPS-010 Offline Lifecycle | 2.0d | 新 lease 不被旧 timeout 删除 |
| OPS-011 Notification Escalation | 2.0d | Task 持久化 + retry |
| OPS-012 Inspection | 3.0d | plan/date 幂等生成 |
| OPS-013 Spare Parts | 2.5d | 库存不为负 + requestId 幂等 |
| OPS-014 Attachments | 1.5d | tenant/assignee/file safety |
| OPS-015 Technician UniApp | 2.5d | 工单/巡检/附件可操作 |
| QA-OPS-004 | 1.5d | race/SQL/syntax/docs gate |
| **Total** | **15d** | |

从 SPEC 7.9 起 Production V1 正式基线为 **50 周 / 250 人日**。
