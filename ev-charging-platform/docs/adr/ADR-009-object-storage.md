# ADR-009 Object Storage

SPEC 7.1 的 Docker 开发环境仍提供固定版本 MinIO，目的是给 S3-compatible 接口、本地账单归档、固件和附件开发提供最小环境。

由于 MinIO 主仓库在 2026 年已经归档，**生产选型不得自动继承开发环境**。进入生产设计前必须重新评估 S3 兼容对象存储（云厂商 S3/OSS/COS、Ceph RGW 或其他持续维护方案），业务代码只依赖对象存储抽象。
