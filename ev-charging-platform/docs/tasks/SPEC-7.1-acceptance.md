# SPEC 7.1 Release Gate

只有以下全部完成，SPEC 7.1 才能从 `foundation-preview` 升为 `foundation-verified`：

- [ ] `mvn clean verify` PASS
- [ ] Testcontainers MySQL integration test PASS
- [ ] `docker compose config` PASS
- [ ] Core infrastructure containers healthy
- [ ] Asset Service starts and Flyway migration succeeds
- [ ] Station API inserts Station + Outbox in one transaction
- [ ] Kafka Outbox publisher → Core consumer PASS
- [ ] RabbitMQ Core command producer → IoT consumer PASS
- [ ] IoT Netty TCP server accepts Device Simulator
- [ ] Redis online key TTL verified
- [ ] Admin `npm run build` PASS
- [ ] Merchant `npm run build` PASS
- [ ] CI workflow green

当前生成环境不能执行 Maven/Docker dependency-level validation，因此本包状态明确标记为 **foundation-preview**。
