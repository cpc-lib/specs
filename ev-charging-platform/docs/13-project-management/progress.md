# Implementation Progress — SPEC 7.5

## 已进入工程实现

- Foundation / Nacos / Gateway / Flyway / Kafka / RabbitMQ
- Station / Charger / Connector
- Netty Device Gateway / Route Lease / Simulator
- ChargingSession / Start / Stop / Telemetry / Recovery / WebSocket
- 峰平谷 Billing / Billing Snapshot / Segment / Replay
- ChargeOrder
- PaymentOrder / MockPayment / Callback 幂等 / UNKNOWN Recovery 骨架
- 部分退款预占
- Payment Event Outbox
- Core Payment Projection Inbox
- Finance 双式 Ledger / Finance Inbox
- Admin Payment / Ledger 页面

## 下一阶段

SPEC 7.6：Reconciliation & Settlement Slice

- 渠道账单导入
- Payment/Channel/Ledger 三方匹配
- 差错单
- Settlement Source
- Settlement Rule Version
- Settlement Engine
- 财务 Replay / 守恒测试

## SPEC 7.6
Finance Reconciliation + Settlement vertical slice entered RC. See `progress-7.6.md`.


## SPEC 7.7
Finance Hardening RC: T+1 reconciliation, raw bill archive, Adjustment/Reversal, settlement approval/ledger posting, Invoice/Red Flush. See `progress-7.7.md`.


## SPEC 7.9
Operation Hardening entered RC. Production baseline revised to **50 weeks / 250 person-days**. See `progress-7.9.md`.


## SPEC 8.0
Product MVP entered RC. Product phase consumes the existing W30-W37 / 40 person-day budget; total remains **50 weeks / 250 person-days**. See `progress-8.0.md`.


## SPEC 8.1
Product Hardening entered RC. Security session lifecycle, full Merchant Station projections, Driver realtime, and Product E2E matrix were added. Total baseline remains **50 weeks / 250 person-days**.


## SPEC 8.2
OpenAPI + Regulatory Integration entered RC. Partner HMAC, callback, full server-side Station Scope, regulatory adapter/task pipeline and outbound security were added. Total baseline remains **50 weeks / 250 person-days**.


## SPEC 8.3
Security/Performance/Chaos hardening entered RC. Sentinel, bounded resources, Prometheus/SLO, performance tests, chaos scripts and OpenAPI master-key rotation were added. Total remains **50 weeks / 250 person-days**.
