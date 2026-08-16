# SPEC 7.2 Release Notes

## Classification

`foundation-rc`

SPEC 7.2 is the first release-candidate foundation after the SPEC 7.0/7.1 quality review. It deliberately does not claim full runtime verification.

## Major corrections from 7.1

- Corrected Testcontainers 2.x MySQL imports and module usage.
- Replaced Nacos import-check bypass with Spring Config Import integration.
- Added Spring Cloud LoadBalancer to Gateway `lb://` routing.
- Added centralized development RequestContext / tenant bridge.
- Added Snowflake ID generator and tests.
- Hardened tenant-scoped Station access.
- Hardened transactional Outbox with lease/claim/retry/dead states.
- Added durable Core Inbox + Station projection.
- Implemented RabbitMQ device command delivery to live Netty channels.
- Added command expiry and RabbitMQ DLQ topology.
- Hardened device authentication and tenant-scoped online status.
- Fixed stale-connection reconnect race in DeviceChannelRegistry.
- Upgraded simulator to duplex command/ACK behavior.
- Added actual Admin Station create/list page.
- Added separate Merchant read-only Station API/page.
- Corrected frontend dependency pins to published versions.
- Added Maven 3.9.16 bootstrap with SHA-512 validation.
- Expanded CI/static/release-gate checks.

## Executed locally in artifact environment

- static project validator: PASS
- pure Java 21 compile: PASS
- backend main Java syntax: PASS
- backend test Java syntax: PASS
- TypeScript/TSX parse: PASS
- shell syntax: PASS
- domain harness: PASS
- ZIP integrity: PASS

## External release blockers

The current artifact execution environment cannot provide a Docker runtime and cannot resolve/download Maven/npm dependencies reliably. Therefore these gates remain external:

- Maven `clean verify`
- Testcontainers execution
- Docker Compose runtime health
- Nacos registration/discovery runtime
- Kafka/RabbitMQ live flows
- Netty/Simulator live flow
- frontend dependency install/build

Run `docs/tasks/SPEC-7.2-release-gate.md` before changing status to `foundation-verified`.
