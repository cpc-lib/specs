# Release Gates

每一个 Sprint 结束必须满足：

1. `cd backend && mvn clean verify` 通过。
2. Flyway 在干净 MySQL 实例可从 0 迁移。
3. 关键写接口具备幂等测试。
4. MQ Consumer 有 Inbox 或等价幂等机制。
5. 金额场景不存在浮点运算。
6. Admin/UniApp Build 通过。
7. E2E 场景形成可重复脚本。
8. SPEC / ADR / Task Estimate 同步更新。

Payment Sprint 额外要求：重复成功回调不会重复更新订单、不会重复记 Ledger；部分退款并发不能超出可退余额。

## SPEC 7.7 Finance Hardening Gate

除通用 Gate 外，还必须满足：

1. T+1 对账没有渠道账单时不得提前制造 LOCAL_ONLY。
2. 原始渠道账单必须在 Normalize 前完成 hash/archive。
3. Adjustment 不得 UPDATE 原始 Payment/Refund Fact。
4. Adjustment / Settlement 必须满足 Maker-Checker。
5. 同一 Adjustment 不得存在多个活动 Reversal。
6. Settlement Source 在审批前只能到 `ALLOCATED`。
7. Settlement Approval Ledger Posting 必须 Debit = Credit。
8. Settlement Reject 必须把 Source 恢复为 READY，且历史 Order 不删除。
9. 同一 Payment 只能存在一个活动 Invoice。
10. Invoice Provider 调用必须在 DB Transaction 外。
11. 成功开票/红冲的相同 requestId 重试必须返回原结果。


## Operation Hardening Gate — SPEC 7.9

必须通过：

- IoT heartbeat lease race E2E
- 旧连接 timeout 不能删除新连接
- OFFLINE/ONLINE lifecycle Kafka E2E
- DEVICE_OFFLINE 默认不自动建单
- Notification retry / DEAD 状态
- Inspection plan/date 幂等
- concurrent spare consume 不得负库存
- attachment tenant + assignee isolation
- Technician UniApp 真机/浏览器构建
- Flyway V1.1.0 空库重放


## Product MVP Gate — SPEC 8.0

### Authentication / authorization

- bearer token login for Admin/Merchant/Driver/Technician
- Driver token cannot access Admin/Merchant/Technician surfaces
- Merchant token cannot access Admin surface
- dev tenant/user headers disabled by default
- demo users disabled by default
- internal Feign uses separate service key
- station DataScope enforced or fails closed

### Admin

- dashboard domain fan-out works
- RBAC/System page works
- no frontend identity-header spoofing
- finance maker-checker uses two real accounts

### Merchant

- overview/stations/orders/payments/settlement/operation
- STATION-scoped Asset/Core E2E
- unsupported station-scoped local projections return forbidden

### Driver

- public station discovery/detail
- MEMBER login
- start/stop charge
- order query
- payment initiation

### Technician

- bearer login
- assigned work order/inspection access
- attachment upload scope

### Runtime

- `cd backend && mvn clean verify`
- Admin `npm install && npm run build`
- Merchant `npm install && npm run build`
- Driver UniApp build
- Technician UniApp build
- browser/mobile E2E


## Product Hardening Gate — SPEC 8.1

### Identity

- refresh token rotates once
- old refresh token replay fails
- logout invalidates access across services
- password change revokes all user sessions
- role/DataScope change revokes all affected user sessions
- 5 failed logins persist and lock account

### Internal API

- anonymous internal API → 401
- user access token → 403
- valid SERVICE identity → allow

### Station DataScope

MERCHANT_STATION sees only assigned stations in:

- Asset
- Core
- Payment
- Finance
- Operation

Historical projection repair must never invent station IDs.

### Driver realtime

- another member cannot issue a ticket for someone else's session
- ticket one-time use
- Gateway WebSocket telemetry works
- polling fallback works

### Product UX

- 401 → one refresh attempt
- refresh failure → login
- 403/409 shown as explicit state
- dashboard loading/error states


## OpenAPI + Regulatory Gate — SPEC 8.2

### Partner Authentication

- valid canonical HMAC request succeeds
- body/query tampering fails
- timestamp outside ±300 seconds fails
- nonce replay fails
- AppSecret rotation immediately invalidates old signatures
- requests/minute rate limit is enforced by Redis
- secret plaintext never appears in DB/audit logs

### Partner Authorization

- required API scope is enforced
- STATION partner sees only assigned stations
- out-of-scope connector remote start is rejected using authoritative Asset connector context
- one partner cannot query another partner's shadow-user order
- partner request IDs cannot collide across partners

### Callback

- callback HMAC can be independently verified
- callback HTTP is outside business transaction
- PENDING/SENDING/RETRY/SENT/DEAD lifecycle works
- stale SENDING claim is recoverable
- manual DEAD retry works

### Regulatory

- unchanged snapshot is payload-hash idempotent
- public station report uses Part-2 canonical profile marker
- business order report uses Part-3 canonical profile marker
- generic adapter is never labeled certified
- failed regulator HTTP never mutates charging/payment/financial facts
- platform rate limit works
- platform-specific adapter can replace generic adapter without changing domain services

### Outbound Security

Under production configuration:

- HTTPS only
- host allowlist mandatory
- localhost/private literal targets denied
- URL revalidated at dispatch time
- deployment egress policy reviewed

### Runtime

- Maven full build
- Flyway open schema migration
- Redis nonce/rate-limit E2E
- real HMAC request E2E
- Partner Station Scope E2E
- callback receiver E2E
- regulator mock-server E2E
- Admin React build


## Security + Performance + Chaos Gate — SPEC 8.3

### Resilience
- Gateway Sentinel returns overload response rather than cascading failure
- charging/payment hot-path rules are active
- bounded executor queue saturation is visible
- no unbounded async queue/thread creation
- Feign/DB/MQ timeouts are finite

### Performance
- public station read failure <1%, p95 <300ms, p99 <800ms on recorded hardware
- OpenAPI read failure <1%, p95 <500ms, p99 <1s on recorded hardware
- telemetry 5k msg/s sustained baseline test
- saturation point and scale trigger recorded

### Chaos
- MySQL outage produces no half-commit
- Redis outage does not bypass token revocation
- Kafka outage grows recoverable Outbox debt
- RabbitMQ outage grows recoverable command debt
- duplicate payment/event x100 yields one logical effect

### Observability
- Prometheus scrape succeeds on every service
- HTTP histogram present
- Hikari and executor gauges present
- alert expressions load successfully

### Security
- old OpenAPI key can decrypt while present in key ring
- rewrap moves ciphertext to active key ID without plaintext return
- previous key removal occurs only after rewrap verification
