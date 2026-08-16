# Validation — SPEC 8.3 Security + Performance + Chaos RC

Status: `foundation-rc-security-performance-chaos`

## Passed in the current execution environment

- `STATIC_VALIDATION=PASS`
- `JDBC_PLACEHOLDER_CHECK=PASS` — **208** JdbcTemplate update calls checked
- `CAPACITY_MODEL=PASS`
- `BILLING_GOLDEN_HARNESS=PASS`
- `DEVICE_ROUTE_LEASE_HARNESS=PASS`
- `PAYMENT_LEDGER_HARNESS=PASS`
- `PURE_JAVA_DOMAIN_HARNESS=PASS`
- `FINANCE_HARDENING_HARNESS=PASS`
- `OPERATION_HARDENING_HARNESS=PASS`
- `PRODUCT_HARDENING_HARNESS=PASS`
- `OPENAPI_SECURITY_HARNESS=PASS`
- `JAVA_SYNTAX_PARSE=PASS` — **279** Java source files; semantic dependency resolution intentionally excluded
- `TYPESCRIPT_SYNTAX_PARSE=PASS` — **39** Admin/Merchant TS/TSX sources; semantic module resolution intentionally excluded
- `VUE_SCRIPT_SYNTAX=PASS` — **13** Driver/Technician Vue script blocks
- `SHELL_SYNTAX=PASS`
- ZIP integrity is checked during packaging

## Capacity-model result

MVP workload model:

- online devices: 10,000
- concurrent charging: 2,000
- telemetry: 5,000 messages/second
- telemetry events/day: 432,000,000
- assumed raw telemetry size: 700 bytes/event
- raw volume before compression: approximately 302.4 GB/day
- 1.5x-headroom wire estimate: approximately 42 Mbps

These are arithmetic sizing inputs, not a live throughput benchmark.

## SPEC 8.3 invariants checked statically

### Sentinel / overload

- MVC services include Spring Cloud Alibaba Sentinel starter.
- Gateway includes Sentinel starter and Sentinel Gateway integration.
- Gateway route rules are finite and configuration driven.
- hot resources exist for charging start/stop and payment/refund.
- Gateway URL filter is disabled in favor of Gateway route resources.
- overload response is represented as HTTP 429 at the MVC boundary.

### Bounded resource use

- async executors have finite max threads and finite queues.
- rejection policy is AbortPolicy.
- callback/regulatory scanners submit only into bounded executor capacity.
- rejected scanner work remains unclaimed in the database.
- Feign modules have finite connect/read timeouts.
- datasource modules have finite Hikari pool limits/timeouts.
- Tomcat thread/backlog values are finite.
- Kafka/Rabbit runtime timeout/prefetch defaults are bounded where configured.

### Observability

- Prometheus registry dependency is present.
- `/actuator/prometheus` is exposed.
- HTTP request histogram/SLO buckets are enabled.
- executor active/pool/queue gauges exist.
- Hikari/JVM/HTTP/executor alert-rule examples exist.
- health probes are enabled.

### Security

- MVC/Gateway security response headers exist.
- OpenAPI ciphertext is upgraded from legacy `v1:` to key-aware `v2:<keyId>:` format.
- active/previous OpenAPI master keys are supported.
- rewrap operation never returns secret plaintext.
- key rotation runbook requires rewrap before old-key removal.

### Performance / Chaos

- k6 public station read scenario exists.
- k6 signed OpenAPI read scenario exists.
- concurrent charging idempotency scenario exists.
- MySQL/Redis/Kafka/RabbitMQ/Nacos outage scripts exist for Bash and PowerShell.
- post-chaos invariant SQL and chaos matrix exist.

## Important defects / risks found while hardening

1. External callback/regulatory dispatch already used persisted claims, but scanner submission had no explicit local backpressure. SPEC 8.3 adds bounded executor submission; saturation leaves tasks unclaimed for the next scan.
2. Service thread/pool limits were mostly framework defaults. SPEC 8.3 makes the RC limits explicit and configuration driven.
3. OpenAPI master-key ciphertext from SPEC 8.2 did not include a key ID. SPEC 8.3 introduces `v2:<keyId>:` plus an explicit safe migration/rewrap procedure.
4. Two overlapping MVC exception-advice classes handled IllegalArgumentException/IllegalStateException. The older advice is now narrowed to validation and duplicate-key errors to avoid ambiguous handlers.
5. Gateway Sentinel setup is kept at route-level; URL-level Sentinel servlet filtering is disabled for the Gateway path.

## Runtime release gates — NOT passed in this environment

### Maven

Actual command:

```bash
./mvnw -v
```

Actual result:

```text
curl: (6) Could not resolve host: downloads.apache.org
```

Therefore Maven dependency bootstrap and `mvn clean verify` cannot be marked PASS.

### Docker

Actual command:

```bash
docker --version
```

Actual result:

```text
docker: command not found
```

Therefore no live infrastructure or chaos script was executed.

### npm

Actual Admin dry run:

```bash
npm install --ignore-scripts --no-audit --no-fund --package-lock=false --dry-run
```

Actual result:

```text
404 Not Found from the current sandbox internal npm registry for @types/react@19.2.17
```

Therefore React dependency install/build cannot be marked PASS.

### k6

No k6 runtime benchmark is claimed in this validation report.

The provided scripts are release-gate scenarios to run on recorded target hardware.

## Required before `foundation-verified`

- Maven full compile/test with Sentinel 1.8.9 resolved by the SCA BOM
- Gateway Sentinel live 429 test
- MVC hot-path Sentinel live 429/circuit-break test
- Prometheus scrape and alert-rule validation
- Hikari/executor saturation tests
- public station and OpenAPI k6 runs with hardware/JVM metadata
- sustained 5k telemetry msg/s run
- MySQL/Redis/Kafka/RabbitMQ/Nacos chaos matrix
- duplicate event/payment callback x100 E2E
- OpenAPI key-ring rotation/rewrap E2E
- Admin/Merchant production frontend builds
- final production capacity report and scale recommendation
