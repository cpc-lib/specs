# SPEC 7.4 Charging Hardening

## Objective

Harden the first charging vertical slice before Payment development.

## Delivered

- [x] Time-of-use BillingEngine
- [x] Billing Version / Period / Station Binding
- [x] Immutable BillingSnapshot
- [x] BillingSegment / BillingResult persistence
- [x] Golden billing harness/tests
- [x] Billing Replay dry-run
- [x] Billing Admin publishing UI/API
- [x] Session Recovery scanner + separate REQUIRES_NEW worker
- [x] QUERY_TRANSACTION Simulator recovery
- [x] Device event timestamps and offline local charging replay
- [x] WebSocket realtime stream
- [x] Single-use WebSocket ticket authorization
- [x] Charger / Connector Admin page
- [x] Realtime Charging Admin page
- [x] Start/Stop transaction boundary hardening
- [x] RabbitMQ publisher confirm + mandatory-return command publication
- [x] Gateway-specific device command queues
- [x] Redis `gatewayId|connectionToken` route lease
- [x] Cross-gateway stale-disconnect compare-and-delete protection

## External Release Gate

Still mandatory before `foundation-verified`:

- [ ] `cd backend && ./mvnw clean verify`
- [ ] Live MySQL + Flyway + Testcontainers
- [ ] Nacos registration/discovery
- [ ] RabbitMQ route-aware command E2E
- [ ] Kafka device event E2E
- [ ] Simulator disconnect/recover E2E
- [ ] WebSocket through Gateway E2E
- [ ] Admin/Merchant `npm install && npm run build`
- [ ] Generate and commit frontend lockfiles
