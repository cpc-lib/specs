# SPEC 8.3 Performance Tests

## Tools

k6 scripts are provided under `tests/performance/k6`.

The package does not bundle a k6 binary.

## First release gates

### Public station read

```bash
k6 run tests/performance/k6/public-station-read.js
```

Gate:

- failure < 1%
- p95 < 300ms
- p99 < 800ms

### Partner OpenAPI station read

Requires:

- `OPEN_APP_KEY`
- `OPEN_APP_SECRET`

Gate:

- failure < 1%
- p95 < 500ms
- p99 < 1s

### Charging start idempotency

Requires:

- real Driver bearer token
- an online simulated connector

The purpose is not maximum QPS. It validates saturation behavior and repeated request IDs while concurrency is present.

## Reporting

Store each benchmark result with infrastructure metadata described in `docs/19-hardening/slo-and-capacity.md`.
