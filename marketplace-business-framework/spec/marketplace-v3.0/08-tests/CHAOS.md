# Chaos Tests
- Redis unavailable
- RabbitMQ unavailable
- Kafka lag
- ES unavailable
- one MySQL shard unavailable
- payment provider timeout
- refund provider timeout
- payout provider timeout
- duplicate/out-of-order events
- settlement consumer restart
Expected: no duplicate money, no oversell, no cross-merchant leakage, recovery tasks visible.
