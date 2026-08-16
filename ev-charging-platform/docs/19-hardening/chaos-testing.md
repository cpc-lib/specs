# Chaos Testing

## Supported RC scenarios

Scripts:

- `mysql-outage`
- `redis-outage`
- `kafka-outage`
- `rabbitmq-outage`
- `nacos-outage`

Linux/macOS:

```bash
scripts/chaos/chaos.sh redis-outage 30
```

Windows:

```powershell
scripts\chaos\chaos.ps1 -Scenario redis-outage -DurationSeconds 30
```

## Safety

Run against disposable integration environments only.

Do not run chaos scripts against production by default.

## Expected behavior

### MySQL outage

- writes fail quickly instead of hanging indefinitely
- no half-committed session/payment effect
- after recovery, no orphan `connector_active_session`

### Redis outage

Authentication revocation is fail-closed.

A Redis outage can therefore reduce availability intentionally rather than allow a revoked token through.

### Kafka outage

- domain transaction remains committed only with Outbox fact
- Outbox remains retryable
- after recovery, publishing converges

### RabbitMQ outage

- charging command Outbox retains delivery debt
- no fake device success is generated
- recovery retries

### Nacos outage

- running instances should continue with already-loaded configuration
- new discovery/config operations may fail
- outage must not corrupt business facts

## Pass criteria

Dependency restart alone is not PASS.

Run `post-chaos-check.sql`, domain E2E, and queue/lag checks after recovery.
