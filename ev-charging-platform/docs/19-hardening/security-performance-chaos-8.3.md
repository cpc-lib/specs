# SPEC 8.3 — Security + Performance + Chaos Hardening

Status: `foundation-rc-security-performance-chaos`

## Objective

Freeze production resilience constraints before Kubernetes/CI-CD rollout.

SPEC 8.3 focuses on:

- overload protection
- bounded resource usage
- observability
- performance baselines
- chaos/recovery behavior
- key rotation
- release gates

It does not add a new business bounded context.

## Sentinel

The project baseline remains Spring Cloud Alibaba 2025.0.0.0.

The official 2025.x release matrix still maps this line to Sentinel 1.8.9.

Protection layers:

```text
Internet
  ↓
Gateway Sentinel route QPS
  ↓
MVC Sentinel resource protection
  ↓
Bounded thread pools
  ↓
Feign / DB / MQ timeouts
  ↓
Domain transaction
```

Gateway default route QPS limits are deliberately configuration values, not magic production constants.

Default RC values are conservative starting points and must be replaced by load-test results.

## Bounded resources

### Servlet

- max threads: 200
- min spare: 20
- accept backlog: 200
- connection timeout: 5s

### Hikari

- minimum idle: 5
- maximum pool: 30
- connection timeout: 3s
- validation timeout: 1s
- idle timeout: 10m
- max lifetime: 30m

### Async executors

`ioBoundedExecutor`

- core: 8
- max: 32
- queue: 500

`businessBoundedExecutor`

- core: 8
- max: 24
- queue: 1000

Queue saturation rejects new async submission rather than silently creating an unbounded memory backlog.

For persisted external tasks this is safe because a rejected scanner submission leaves the task unclaimed and the next scan can retry it.

## Feign

Default RC budget:

- connect: 2s
- read: 5s

A service-specific API may set a stricter timeout.

Do not globally increase read timeouts to hide a slow dependency.

## Observability

Prometheus endpoints are exposed through Spring Boot Actuator.

Important metrics:

- `http_server_requests_seconds_*`
- JVM memory / GC
- Hikari active/max
- `ev_executor_active`
- `ev_executor_queue_size`
- `ev_executor_queue_remaining`
- Kafka consumer lag
- RabbitMQ queue depth
- domain outbox/inbox debt
- payment UNKNOWN count
- callback/regulatory DEAD count

## Security

SPEC 8.3 adds:

- API security response headers
- Gateway security response headers
- production outbound egress policy from SPEC 8.2
- OpenAPI master-key key ring
- secret ciphertext key ID
- zero-plaintext secret rewrap operation

## Chaos principle

Chaos tests are not successful because a dependency was killed.

They pass only when, after recovery:

- state-machine invariants still hold
- no duplicated money effect appears
- no connector has two active sessions
- Outbox/Inbox debt converges
- payment UNKNOWN converges through query/recovery
- callback/regulatory delivery converges or enters explicit DEAD state
