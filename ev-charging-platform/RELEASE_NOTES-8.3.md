# Release Notes — SPEC 8.3 Security + Performance + Chaos

Status: `foundation-rc-security-performance-chaos`

## Resilience
- Sentinel MVC starter
- Sentinel Gateway route flow control
- explicit charging/payment hot-path resources
- circuit-breaker baseline
- bounded async executors
- scanner backpressure
- Feign/Hikari/Tomcat/Kafka/Rabbit bounded runtime defaults

## Observability
- Prometheus registry
- HTTP latency histograms and SLO buckets
- executor queue metrics
- scrape configuration
- alert-rule baseline
- liveness/readiness probes

## Security
- MVC/Gateway security response headers
- OpenAPI AES master-key key ring
- `v2:keyId:ciphertext`
- safe tenant secret rewrap endpoint

## Performance
- k6 public station read
- signed OpenAPI read
- charging idempotency concurrency scenario
- capacity model for 10k online / 2k charging / 5k telemetry per second

## Chaos
- MySQL / Redis / Kafka / RabbitMQ / Nacos outage scripts
- PowerShell equivalent
- post-chaos invariant SQL
- chaos acceptance matrix

## Schedule
SPEC 8.3 consumes the planned **W40-W42 / 15 person-days**.
Production V1 remains **50 weeks / 250 person-days**.
