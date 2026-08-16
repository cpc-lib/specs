# EV Charging Platform — 一人 + AI 工期基线

Production V1 当前规划：**50 周 / 250 人日**。

## 为什么从 47 周调整到 50 周

SPEC 7.9 把此前 P8 中仅列名的能力真正纳入 Production V1：

- Device Offline Detection
- Notification Escalation
- Inspection Plan / Task
- Spare Parts Inventory
- WorkOrder Attachments
- Technician UniApp

这些不是“顺手 CRUD”，涉及连接竞态、SLA/通知、库存并发、文件安全和现场技师体验。

因此新增：

**S8B Operation Hardening — W27-W29 / 15 人日**

并将后续阶段顺延 3 周，原有 4 周真实联调 Buffer 保持不变。

## 快速入口

- `docs/13-project-management/roadmap.md`
- `docs/13-project-management/milestones.md`
- `docs/13-project-management/sprint-plan.md`
- `docs/13-project-management/task-estimates.md`
- `docs/13-project-management/release-gates.md`
- `docs/13-project-management/one-person-ai-development-plan.md`
- `docs/13-project-management/progress-7.9.md`

## 最新路线

| 阶段 | 周期 | 人日 | 主要产出 |
|---|---:|---:|---|
| Foundation + Asset | W1-W4 | 20 | 工程底座、资产 |
| IoT + Charging + Billing | W5-W14 | 50 | 设备、充电、计费、订单 |
| Payment | W15-W17 | 15 | 支付退款 |
| Finance + Invoice | W18-W23 | 30 | Ledger、对账、结算、发票 |
| Operation Vertical Slice | W24-W26 | 15 | Alarm、WorkOrder、Flowable、SLA |
| **Operation Hardening** | **W27-W29** | **15** | Offline、通知、巡检、备件、附件、Technician App |
| React Admin + Merchant | W30-W33 | 20 | 后台完整化 |
| Driver UniApp | W34-W37 | 20 | 用户 App |
| OpenAPI / Regulatory | W38-W39 | 10 | 第三方与监管 |
| Security / Performance | W40-W42 | 15 | 安全、容量、Chaos |
| K8s / DR / Release | W43-W44 | 10 | 发布与灾备 |
| AI Ops + Packaging | W45-W46 | 10 | AI 运维、交付文档 |
| Integration Buffer | W47-W50 | 20 | 真桩、支付、账单、发票、监管联调 |

## 当前 SPEC 7.9 进度

Operation Hardening 已进入 RC：

- IoT heartbeat timeout/offline lifecycle：RC
- Offline Alarm 自动恢复：RC
- Notification escalation：RC
- Inspection：RC
- Spare parts：RC
- Attachments：RC
- Technician UniApp：RC
- Admin operation hardening pages：RC

总计划从本版本起以 **50 周 / 250 人日** 为新的正式基线。


## SPEC 8.0 当前进度

Product MVP 正式进入 RC，对应既定：

- W30-W33 Admin + Merchant / 20 人日
- W34-W37 Driver UniApp / 20 人日

本版没有新增总范围，因此 Production V1 **仍保持 50 周 / 250 人日**。

当前已落地：

- Product IAM / signed token
- RBAC surface roles
- DataScope model
- Internal Service Context
- Admin authenticated shell/dashboard/system
- Merchant portal
- Driver core journey
- Technician bearer-token migration

剩余工作主要进入真实 Runtime/UX 验收，而不是继续增加后端业务域。


## SPEC 8.1 当前进度

SPEC 8.1 Product Hardening 使用 **W30-W37 Product MVP 已有 40 人日预算中的 15 人日**，不新增总工期。

本版子预算：

- Access / Refresh / Revocation：3d
- Permission / Role / DataScope：3d
- Station Projection + Backfill：3d
- Driver WebSocket：2d
- Frontend Resilience：2d
- E2E / Security Review / Docs：2d

因此 Production V1 正式基线继续保持：

**50 周 / 250 人日**

W47-W50 的 4 周外部真实联调 Buffer 仍完整保留。


## SPEC 8.2 当前进度

SPEC 8.2 正式进入原计划：

**W38-W39 / OpenAPI + Regulatory / 10 人日**

子预算：

| 工作 | 人日 |
|---|---:|
| HMAC / nonce / rate limit / secret storage | 2 |
| Partner API + Station DataScope | 2 |
| Callback / Audit / Recovery | 1.5 |
| Regulatory Adapter / Report Pipeline | 2 |
| Admin UI / OpenAPI Contract | 1 |
| E2E / Security Review / Docs | 1.5 |
| **合计** | **10** |

该阶段已经包含在原有 50 周计划中，因此：

**Production V1 仍保持 50 周 / 250 人日。**

W47-W50 的 4 周外部真实联调 Buffer 不被占用。


## SPEC 8.3 当前进度

SPEC 8.3 对应既定 **W40-W42 / 15 人日**：

- Sentinel / Overload Protection：3d
- Bounded Pool / Timeout / Backpressure：2d
- Prometheus / SLO / Alerts：2d
- Load Test / Capacity Model：3d
- Chaos / Recovery：3d
- Security / Key Rotation：2d

因此 Production V1 仍保持 **50 周 / 250 人日**，W47-W50 外部真实联调 Buffer 不变。
