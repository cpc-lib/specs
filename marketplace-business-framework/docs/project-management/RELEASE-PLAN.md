# Release Plan

| Version | 目标时间 | 主要范围 | 对应 TASK |
|---|---:|---|---|
| V0.1 | Week 4 | Foundation / System / ShardingSphere 设计 / 三端基线 | TASK-001 |
| V0.2 | Week 10 | User / Merchant / Risk | TASK-002, 003, 029 |
| V0.3 | Week 18 | Product / Pricing / Inventory / Shop / Moderation | TASK-004, 005, 006, 007, 008, 030 |
| V0.4 | Week 24 | Promotion / Cart / Search / Checkout | TASK-009, 010, 011, 027 |
| V0.5 | Week 36 | Trade / Payment / Saga / Fulfillment / Logistics | TASK-012, 013, 014, 015, 016 |
| V0.6 | Week 44 | AfterSale / Refund / Dispute | TASK-017, 018, 019, 020 |
| V0.7 | Week 56 | Settlement / Finance / Reconciliation / Invoice | TASK-022, 023, 024, 025, 026 |
| V0.8 | Week 62 | Review / Notification / Recommendation | TASK-021, 028, 031 |
| V0.9 | Week 70 | Admin / Flash Sale / Procurement / 全量前端 | TASK-032, 033, 034, 035, 036 |
| V0.95 | Week 78 | Production Hardening | TASK-037, 038 |
| V1.0 | Week 80 | Production Release | RC / V1.0 |

## Release Gate

任何版本进入 release 前至少满足：

- 对应 SPEC traceability 可追踪
- DDL / Flyway 已落地且分片键正确
- API contract 已冻结
- 对应三端页面已完成或明确 N/A
- P0 / P1 Bug = 0
- 核心失败路径已有测试
- 权限验证完成
- 数据迁移可执行
- 跨服务集成验证通过
- Release note 已更新

## 版本节奏对比

| 维度 | 旧计划 | 新计划 |
|---|---|---|
| 总版本数 | 11 (V0.1-V1.0) | 11 (V0.1-V1.0) |
| 总周期 | 96 周 | 80 周 |
| 平均版本间隔 | ~8.7 周 | ~7.3 周 |
| V0.5 (Trade/Payment) | Week 50 | Week 36 (省 14 周) |
| V0.7 (Settlement/Finance) | Week 78 | Week 56 (省 22 周) |
| V1.0 | Week 96 | Week 80 (省 16 周) |

加速来源：
- AI 并行生成前后端代码（省 ~8 周）
- ShardingSphere 前移避免返工（省 ~4 周）
- SPEC Wave 对齐避免依赖倒置（省 ~4 周）
