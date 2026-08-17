# SPEC 7.2 Release Gate

Status can change from `foundation-rc` to `foundation-verified` only when every mandatory item is green.

## Static

- [ ] `python scripts/validate_static.py`
- [ ] `docker compose -f deploy/docker/docker-compose.yml config`

## Backend

- [ ] `cd backend && ./mvnw -B -ntp clean verify`
- [ ] Asset Testcontainers MySQL tests pass
- [ ] Core Inbox/projection Testcontainers tests pass
- [ ] Spring Boot context tests pass

## Infrastructure runtime

- [ ] MySQL healthy
- [ ] Redis healthy
- [ ] RabbitMQ healthy
- [ ] Kafka healthy
- [ ] Nacos healthy
- [ ] No host-port collision

## Vertical slice

- [ ] Gateway registered/discovers Asset/Core/IoT
- [ ] Admin creates Station through Gateway
- [ ] Station + Outbox commit atomically
- [ ] Outbox event arrives in Kafka
- [ ] Core persists Inbox exactly once logically
- [ ] Core creates RabbitMQ device command
- [ ] IoT routes command to authenticated Netty channel
- [ ] Simulator receives command and ACKs
- [ ] Device online status is tenant-scoped in Redis

## Frontend

- [ ] `admin-web npm run build`
- [ ] `merchant-web npm run build`
- [ ] Admin Station page creates and lists through Gateway
- [ ] Merchant Station page lists tenant-scoped data

## Promotion rule

No unchecked mandatory item => `foundation-verified`.
Any unchecked item => keep `foundation-rc`.
