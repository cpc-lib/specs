# SPEC 8.3 Progress — Security + Performance + Chaos

Status: `foundation-rc-security-performance-chaos`

## RC completed

### Stability

- Spring Cloud Alibaba Sentinel starter on MVC services
- Sentinel Gateway integration
- route QPS rules
- charging/payment hot-path resources
- exception-ratio circuit-breaker baseline
- bounded async executors
- executor queue metrics
- Feign timeout baseline
- Hikari bounded pool baseline
- Kafka/Rabbit timeout/prefetch baseline
- Tomcat thread/backlog bounds

### Observability

- Prometheus registry
- `/actuator/prometheus`
- HTTP latency histograms/SLO buckets
- health liveness/readiness probes
- Prometheus scrape example
- alert-rule baseline

### Security

- MVC/Gateway response security headers
- OpenAPI master-key key ring
- ciphertext key ID
- tenant secret rewrap
- key rotation runbook

### Performance

- 10k / 2k / 5k workload model
- capacity arithmetic
- k6 public read test
- k6 OpenAPI signed read test
- charging idempotency concurrency test
- saturation thresholds

### Chaos

- MySQL / Redis / Kafka / RabbitMQ / Nacos outage scripts
- Windows PowerShell equivalent
- post-chaos invariant SQL
- chaos acceptance matrix

## Schedule

SPEC 8.3 maps exactly to the existing:

**W40-W42 / 15 person-days**

| Work | Person-days |
|---|---:|
| Sentinel / overload protection | 3 |
| bounded pools / timeout/backpressure | 2 |
| Prometheus/SLO/alerts | 2 |
| load-test scenarios / capacity model | 3 |
| chaos/recovery tests | 3 |
| security/key rotation/review | 2 |
| **Total** | **15** |

Production V1 remains **50 weeks / 250 person-days**.

W47-W50 integration buffer remains untouched.
