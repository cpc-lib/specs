# MVP Acceptance

1. Asset Service 启动并执行 Flyway。
2. POST Station 成功，同时写 Outbox。
3. IoT Service TCP 19090 可接受 Simulator。
4. AUTH/PING/TELEMETRY 有响应，Redis Online Key 有 TTL。
5. ChargingSession Unit Test 通过。
6. Admin / Merchant `npm run build` 通过。
7. Docker Compose 不存在 Host Port 冲突。
8. `mvn clean verify` 在具备 Maven + Docker 的开发机/CI 必须通过后才能标记 SPEC 7.1 为 release-ready。
