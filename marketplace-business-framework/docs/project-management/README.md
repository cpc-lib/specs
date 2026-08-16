# Project Management

本目录只管理 **项目怎么推进**，不定义商城业务规则。

业务规则唯一事实源仍然是：

`spec/marketplace-v3.0/`

## 文件职责

| 文件 | 职责 |
|---|---|
| `MASTER-ROADMAP.md` | 项目主路线、阶段和执行原则（v2 修订版，80 周） |
| `Marketplace全量实现_单人AI_24个月计划.md` | 单人 + AI 的 80 周 / 40 Sprint 全量实施计划（含 TASK->Sprint 映射） |
| `MILESTONES.md` | M0～M10 关键里程碑和验收条件 |
| `RELEASE-PLAN.md` | V0.1～V1.0 版本发布计划 |
| `PROGRESS.md` | 当前真实完成情况；每个 Sprint 结束必须更新 |

## 文档优先级

当文件之间发生冲突时：

1. `spec/marketplace-v3.0/00-master/V3.0-FROZEN-CONTRACT.md`
2. `spec/marketplace-v3.0/00-master/MASTER-SPEC-V3.0.md`
3. 各业务模块 `MODULE-SPEC.md`
4. `MASTER-ROADMAP.md`
5. 80 周实施计划
6. `PROGRESS.md`

Roadmap 和工期文档不能修改业务事实，只能安排实现顺序。

## v2 修订要点

- 总周期从 96 周压缩至 80 周（基于 AI 并行生成能力）
- ShardingSphere 从 P9 前移至 P0（避免后期返工）
- Phase 结构严格对齐 SPEC Wave 0-12
- 新增 TASK -> Sprint 完整映射表
- 前端改为 AI 并行生成（取消"仅周五"模式）
- Risk 前移至 Phase 1（遵循 SPEC Wave 2）
- Search 前移至 Phase 3（遵循 SPEC Wave 4）
- Flash Sale 后移至 Phase 8（遵循 SPEC Wave 11）
