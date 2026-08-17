# SPEC 7.5 Release Gate

## Static Gate

- [x] Static structure validation
- [x] JDBC placeholder check
- [x] Pure Java Billing/Route/Payment/Ledger harness
- [x] Java syntax parse
- [x] TypeScript syntax parse
- [x] XML / JSON / YAML parse

## Runtime Gate

- [ ] `cd backend && mvn clean verify`
- [ ] Flyway clean MySQL migration
- [ ] Nacos service discovery
- [ ] Kafka Payment Outbox → Core / Finance consumers
- [ ] 100 concurrent duplicate payment callbacks → one logical effect
- [ ] Concurrent partial refund does not over-refund
- [ ] Finance Ledger event idempotency
- [ ] Docker Compose runtime
- [ ] Admin `npm install && npm run build`

只有 Runtime Gate 全绿后，状态才能从 `foundation-rc-payment-ledger` 升级为 `foundation-verified-payment-ledger`。
