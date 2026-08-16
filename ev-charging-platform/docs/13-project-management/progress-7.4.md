# One Person + AI Progress - SPEC 7.4

## Baseline schedule

The production V1 baseline remains **47 weeks / approximately 235 person-days** for one developer + AI.

Intermediate targets remain:

| Target | Planned duration | Scope |
|---|---:|---|
| Vertical Slice Demo | 12-14 weeks | Asset + IoT + charging + telemetry + billing + order |
| Internal MVP | 22-26 weeks | Adds payment/refund, basic finance, Admin/UniApp, alarms |
| Production V1 | 43 development weeks + 4 buffer weeks | Full enterprise release gate |

## SPEC 7.4 engineering position

SPEC 7.4 represents partial implementation of work normally spread across Sprint 1-4. It does **not** mean 14 calendar weeks have elapsed or that all Sprint 1-4 production exit criteria are complete.

| Area | SPEC 7.4 status |
|---|---|
| Station / Charger / Connector | RC implemented |
| Netty Gateway / stateful Simulator | RC implemented |
| Multi-Gateway command route lease | RC implemented |
| Start / Stop ChargingSession | RC implemented |
| ActiveSession DB uniqueness | RC implemented |
| Device event-time telemetry | RC implemented |
| Disconnect / replay recovery mechanism | RC implemented |
| WebSocket realtime + single-use ticket | RC implemented |
| Peak/Flat/Valley versioned billing | RC implemented |
| BillingSegment / BillingResult | RC implemented |
| Billing Replay | RC implemented |
| ChargeOrder generation | RC implemented |
| Payment / Refund | Next critical path |
| Ledger / Reconciliation / Settlement | Planned |
| Invoice / Wallet | Planned |
| Flowable maintenance | Planned |
| Production charging-pile protocol certification | External dependency |

## Next one-person + AI block

Recommended next block: **10-15 person-days**.

1. Runtime Release Gate on a Docker/network capable Windows/Linux environment: **3-5 days**.
2. Recovery fault-injection E2E and command ACK/result lifecycle: **2-3 days**.
3. Payment domain + MockPayment vertical slice: **3-4 days**.
4. Regression fixes / docs / CI hardening: **2-3 days**.

After this block, move directly into Payment/Refund. Avoid adding unrelated CRUD before the charging transaction chain is runtime-verified.

## One-person + AI daily capacity rule

A sustainable week should budget roughly:

- 50-60% implementation/review.
- 20-25% tests and integration verification.
- 10-15% SPEC/ADR/task maintenance.
- 10-15% defect/risk buffer.

AI should primarily accelerate boilerplate, tests, migrations, UI scaffolding, contract consistency checks and review. Human time remains the critical resource for domain decisions, reconciliation correctness, device protocol edge cases, security and release acceptance.
