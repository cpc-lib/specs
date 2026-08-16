# SPEC 7.5 Release Notes

Status: `foundation-rc-payment-ledger`

## Added

- Payment Vertical Slice
- Mock Payment Gateway
- Payment callback idempotency
- Payment Event Outbox → Kafka
- Core Payment Projection Inbox
- Refund reservation / partial refund
- Finance append-only ledger
- Double-entry balance guard
- 一人 + AI Roadmap / Milestone / Sprint / Task Estimate / Release Gates

## Release Gate

仍要求在完整运行环境实际通过 Maven、Docker、MySQL/Flyway、Kafka、Nacos、Testcontainers 与 Payment E2E 后才能升级为 `verified`。
