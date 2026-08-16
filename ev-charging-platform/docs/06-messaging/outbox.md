# Transactional Outbox

StationApplicationService 展示了数据库业务事实与 Outbox 同一个 Spring `@Transactional` 事务提交的最小可运行示例。生产实现下一阶段增加 Outbox Publisher、Kafka Producer、重试、Inbox。

## SPEC 7.4 Device Command Outbox

The command outbox now uses an explicit claim state:

- `0 NEW`
- `9 PUBLISHING`
- `1 PUBLISHED`
- `3 DEAD`

Multiple Core instances race on an atomic `NEW -> PUBLISHING` update. Only the winner publishes. RabbitMQ publisher confirms are required before the row is marked `PUBLISHED`. Stale publishing claims are recovered after 30 seconds and expired commands become `DEAD`.

## SPEC 7.4 RabbitMQ publish gate

Device command outbox uses correlated publisher confirms **and** mandatory publishing.
A row becomes `PUBLISHED` only when the broker ACKs the publish and the message was not returned as unroutable.
`PUBLISHING` is a short claim state so multiple Core instances cannot publish the same NEW row concurrently; stale claims are recovered.

Current RC limitation: command routing key is still `gateway.dev` (single logical gateway queue). Multi-gateway `deviceId -> gatewayId -> routingKey` routing is deferred to the next hardening iteration and is a production release blocker for horizontal IoT gateway scale-out.
