# Milestones & Exit Gates — SPEC 7.9

## M2 Vertical Slice — W14
Station → Charger → Connector → Simulator → Start → Telemetry → Stop → Billing → ChargeOrder。

## M3 Payment MVP — W17
ChargeOrder → PaymentOrder → SUCCESS/UNKNOWN → Callback 幂等 → Refund。

## M4 Finance MVP — W23
PaymentSucceeded → Ledger → Channel Bill → Reconciliation → Settlement → Invoice。

## M5 Operation MVP — W29
Device Alarm/Offline → Alarm Dedup → WorkOrder → Flowable → SLA，并包含通知、巡检、备件、附件与 Technician App。

## M6 Product MVP — W37
Admin + Merchant + Driver UniApp 主要业务页面完整。

## M7 Production Hardening — W46
OpenAPI、权限隔离、安全测试、容量压测、Chaos、K8s、DR、AI Ops 与发布文档达到上线门禁。

## Production Buffer — W47-W50
真实充电桩、支付/账单、发票、监管平台联调阻塞清零。


## SPEC 8.0 RC status

Product MVP assets for M6 are now present in the repository, but M6 is not marked Verified until React/UniApp runtime builds and end-to-end product acceptance pass.


## SPEC 8.2 RC status

The W38-W39 OpenAPI/Regulatory integration assets are now present:

- Partner HMAC/OpenAPI
- callback delivery
- regulatory adapter/task pipeline
- outbound security
- integration E2E matrix

This milestone remains RC until live partner/regulator mock-server acceptance passes.

## SPEC 8.3 RC status

W40-W42 hardening assets are present:

- Sentinel overload protection
- Prometheus/SLO baseline
- bounded executors/timeouts
- performance scenarios
- chaos/recovery scenarios
- OpenAPI master-key rotation

M7 is not marked Verified until the live load/chaos gates run on recorded infrastructure.
