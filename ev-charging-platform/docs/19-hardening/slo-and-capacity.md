# SLO and Capacity Baseline

## MVP workload baseline

The Production V1 sizing target remains:

- 10,000 online charging devices
- 2,000 concurrent charging sessions
- 5,000 telemetry messages/second

This is a workload target, not a claim that one JVM instance can carry the full load.

## Capacity arithmetic

Assumption for first sizing model:

- normalized telemetry payload + Kafka overhead estimate: 700 bytes/event
- headroom: 1.5x

At 5,000 events/s:

- events/day: 432,000,000
- raw data before compression: approximately 302.4 GB/day
- estimated telemetry network rate with 1.5x headroom: approximately 42 Mbps

Therefore telemetry storage retention and ClickHouse compression/partition strategy matter more than a naive "messages per second" number.

## API SLO

### Read API

Target under normal production load:

- success >= 99.9%
- p95 <= 300 ms
- p99 <= 800 ms

### Write / command acceptance

For local command acceptance, excluding device physical execution:

- p95 <= 500 ms
- p99 <= 1 s

### Payment callback

After the channel request reaches the platform:

- local idempotent callback processing p95 <= 500 ms
- no duplicate ledger/business effect

### Device telemetry

Gateway ingestion:

- 5,000 msg/s sustained baseline
- 1.5x short-term headroom target
- backpressure must be observable before heap exhaustion

## Saturation signals

Scale or investigate before:

- CPU > 70% sustained
- JVM heap > 80% sustained
- Hikari active/max > 80% sustained
- bounded executor queue remaining < 20%
- Kafka lag rises continuously for 5 minutes
- RabbitMQ queue has monotonically increasing depth
- HTTP p95 misses SLO for 10 minutes

## Test methodology

Always record:

- hardware / VM size
- JVM Xms/Xmx
- service replica count
- DB size and connection limit
- Redis topology
- Kafka partitions
- test data size
- payload size
- network latency

A QPS result without these values is not a capacity result.
