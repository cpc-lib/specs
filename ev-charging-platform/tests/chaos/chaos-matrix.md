# SPEC 8.3 Chaos Matrix

| ID | Fault | During fault | Recovery assertion |
|---|---|---|---|
| CHAOS-DB-001 | MySQL 30s outage | writes fail fast | no half commit; sessions recover |
| CHAOS-REDIS-001 | Redis 30s outage | auth revocation fails closed | auth resumes; no bypass |
| CHAOS-KAFKA-001 | Kafka 60s outage | Outbox debt grows | debt converges after Kafka returns |
| CHAOS-RMQ-001 | RabbitMQ 60s outage | commands not delivered | command Outbox retries |
| CHAOS-NACOS-001 | Nacos 60s outage | existing instances continue where possible | discovery/config resumes |
| CHAOS-CALLBACK-001 | Partner callback 500/timeout | RETRY grows | SENT or DEAD explicitly |
| CHAOS-REG-001 | Regulator timeout | report RETRY grows | reporting converges; finance unchanged |
| CHAOS-RESTART-001 | Core process restart mid-session | device may continue charging | session recovery converges |
| CHAOS-DUP-001 | duplicate Kafka/payment callback x100 | repeated input | one logical business/ledger effect |

## Evidence required

For each run retain:

- scenario start/end timestamp
- git/SPEC version
- service logs
- Prometheus snapshot
- database invariant queries
- Kafka/Rabbit backlog before/during/after
- final pass/fail conclusion
